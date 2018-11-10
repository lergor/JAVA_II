package ru.ifmo.git.entities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import picocli.CommandLine;

import ru.ifmo.git.commands.GitCommand;
import ru.ifmo.git.structs.*;
import ru.ifmo.git.tree.Tree;
import ru.ifmo.git.tree.TreeEncoder;
import ru.ifmo.git.tree.visitors.*;
import ru.ifmo.git.util.*;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GitManager {

    private GitStructure git;
    private GitFileManager fileManager;
    private GitLogger logger;

    private static final String sep = System.getProperty("line.separator");
    private static final String tab = "\t";

    public GitManager(Path directory) {
        git = new GitStructure(directory);
        fileManager = new GitFileManager(git);
        logger = new GitLogger(git);
    }

    public GitStructure structure() {
        return git;
    }

    public GitLogger logger() {
        return logger;
    }

    public CommandResult executeCommand(CommandLine commandLine) {
        GitCommand command = commandLine.getCommand();
        return command.execute(this);
    }

    public CommandResult init() throws GitException {
        if (!git.exists()) {
            try {
                git.createGitTree();
                logger.writeHeadInfo(new HeadInfo());
                logger.writeUsages(new Usages());
                return new CommandResult(ExitStatus.SUCCESS,
                        "Initialized empty Git repository in " + git.repo());
            } catch (IOException e) {
                return new CommandResult(ExitStatus.FAILURE,
                        "Unable to create repository in " + git.repo(), e);
            }
        } else {
            return new CommandResult(ExitStatus.SUCCESS,
                    "Reinitialized existing Git repository in " + git.repo());
        }
    }

    public CommandResult add(List<Path> files) throws IOException, GitException {
        HeadInfo headInfo = logger.getHeadInfo();
        if (headInfo.merging() && headInfo.mergeConflictFlag()) {
            for (Path p: files) {
                headInfo.getConflictingFiles().remove(git.repo().relativize(p.toAbsolutePath()).toString());
            }
            if(headInfo.getConflictingFiles().isEmpty()) {
                logger().turnOffConflicting();
            }
        }

        for (Tree tree : Tree.createTrees(files, git.repo())) {
            tree.setRoot(git.index());
            tree.accept(new SaverVisitor());
        }
        Tree repository = Tree.createTree(git.repo());
        Tree index = Tree.createTree(git.index());
        String currentTreeHash = logger.currentTreeHash();

        if (currentTreeHash != null && !currentTreeHash.isEmpty()) {
            Tree lastCommit = new TreeEncoder(git.storage()).decode(currentTreeHash);
            StatusInfo statusInfo = new StatusInfo(repository, lastCommit, index);

            for (Tree tree : Tree.createTrees(files, git.repo())) {
                tree.setRoot(git.index());
                if (statusInfo.getDeleted(false).contains(tree.path())) {
                    tree.accept(new DeleteVisitor());
                }
            }
        }
        return new CommandResult(ExitStatus.SUCCESS, "add: done!");
    }

    public CommandResult commit(String message) throws GitException, IOException {
        HeadInfo headInfo = logger.getHeadInfo();
        if (headInfo.merging()) {
            if(headInfo.mergeConflictFlag()) {
                Message msg = new Message();
                msg.write("error: Committing is not possible because you have unmerged files." + sep);
                msg.write("hint: Fix them up in the work tree, and then use 'git add/rm <file>'" + sep);
                msg.write("hint: as appropriate to mark resolution and make a commit." + sep);
                msg.write("fatal: Exiting because of an unresolved conflict." + sep);
                return new CommandResult(ExitStatus.ERROR, msg);
            } else {
                if(message == null || message.isEmpty()) {
                    message = "Merge branch " + headInfo.mergeBranch() + sep;
                }
                logger.turnOffMerging();
            }
        }
        CommitVisitor visitor = new CommitVisitor();
        Tree tree = Tree.createTree(git.index());
        String currentHash = logger.currentTreeHash();

        if (tree.isEmpty() || !currentHash.isEmpty() && currentHash.equals(tree.hash())) {
            return new CommandResult(ExitStatus.SUCCESS,
                    "Nothing to commit, working tree clean");
        }
        tree.accept(visitor);

        CommitInfo commitInfo = logger.fillCommitInfo(message, tree.hash());
        FileReference commit = GitLogger.formCommitReference(commitInfo, tree.info());

        visitor.references().add(commit);
        visitor.saveReferences(git.storage());

        Usages usages = logger.getUsages();
        visitor.addReferencesToUsages(usages);
        logger.writeUsages(usages);

        logger.writeLog(commitInfo);
        logger.changeHeadInfo(commitInfo.hash);
        return new CommandResult(ExitStatus.SUCCESS, "commit: done!");
    }

    public CommandResult log(String revision) throws GitException {
        HeadInfo headInfo = logger.getHeadInfo();
        if (headInfo.currentHash().isEmpty()) {
            return new CommandResult(ExitStatus.SUCCESS, logger.emptyLogResult());
        }
        List<CommitInfo> history = getHistoryFromCommit(revision);
        if (history.size() == 0) {
            String failMessage = "log: '" + revision + "' unknown revision";
            return new CommandResult(ExitStatus.FAILURE, failMessage);
        }
        Message logContent = new Message();
        history.forEach(info -> logContent.write(info.toString()));
        return new CommandResult(ExitStatus.SUCCESS, logContent);
    }

    private List<CommitInfo> getHistoryFromCommit(String revision) throws GitException {
        String headHash = logger.getHeadInfo().headHash;
        return logger.getHistory().stream().filter( new Predicate<CommitInfo>() {
            private boolean include = false;

            @Override
            public boolean test(CommitInfo commitInfo) {
                if(commitInfo.hash().equals(headHash)) {
                    include = true;
                }
                if (commitInfo.hash().equals(revision)) {
                    include = false;
                    return true;
                }
                return include;
            }
        }).collect(Collectors.toList());
    }

    public CommandResult reset(String revision) throws GitException, IOException {
        if (logger.getHeadInfo().mergeConflictFlag()) {
            return new CommandResult(ExitStatus.FAILURE,
                    "Unresolved conflict; fix conflicts first.");
        }
        String failMessage = "reset: '" + revision + "' unknown revision";
        List<CommitInfo> commitsToDelete = getHistoryFromCommit(revision);

        if (commitsToDelete.isEmpty()) {
            return new CommandResult(ExitStatus.FAILURE, failMessage);
        }
        if (fileManager.restoreCommit(revision, git.index())) {
            commitsToDelete.remove(commitsToDelete.size() - 1);
            deleteCommits(commitsToDelete);

            List<CommitInfo> history = logger.getHistory();
            history.removeAll(commitsToDelete);
            logger.replaceLog(history);

            logger.changeHeadInfo(revision);
            return new CommandResult(ExitStatus.SUCCESS, "reset: done!");
        }
        return new CommandResult(ExitStatus.FAILURE, failMessage);
    }

    public CommandResult checkout(String revision) throws GitException, IOException {
        if (logger.getHeadInfo().mergeConflictFlag()) {
            return new CommandResult(ExitStatus.FAILURE,
                    "Unresolved conflict; fix conflicts first.");
        }
        if (Files.exists(git.log().resolve(revision))) {
            return checkoutBranch(revision);
        }
        if (logger.getHeadInfo().currentHash().equals(revision)) {
            return new CommandResult(ExitStatus.FAILURE,
                    "checkout: already on commit " + revision);
        }
        if (fileManager.restoreCommit(revision, git.index())) {

            GitFileManager.clearDirectory(git.repo());

            Tree tree = Tree.createTree(git.index());
            tree.setRoot(git.repo());
            tree.accept(new SaverVisitor());

            String branch = Files.readAllLines(fileManager.correctPath(revision)).get(2);
            logger.changeHeadInfo(revision, branch);
            return new CommandResult(ExitStatus.SUCCESS, "checkout: done!");
        }
        String failMessage = "checkout: '" + revision + "' unknown revision";
        return new CommandResult(ExitStatus.FAILURE, failMessage);
    }

    public CommandResult checkout(List<Path> files) throws IOException, GitException {
        CommandResult successResult = new CommandResult(ExitStatus.SUCCESS, "checkout: done!");
        String lastCommit = logger.currentTreeHash();
        if (lastCommit.isEmpty()) {
            for (Tree t : Tree.createTrees(files, git.repo())) {
                t.accept(new DeleteVisitor());
            }
            return successResult;
        }
        restoreCommitToIndexAndRepo(lastCommit, files);
        return successResult;
    }

    private void restoreCommitToIndexAndRepo(String commit, List<Path> files) throws IOException, GitException {
        Tree lastCommitTree = new TreeEncoder(git.storage()).decode(commit);
        if(files != null) {
            for (Path f : files) {
                String file = git.repo().relativize(f.toAbsolutePath()).toString();
                Tree node = lastCommitTree.find(file);
                if (node == null) {
                    throw new GitException("No such file '" + file + "' found");
                }
                node.setRoot(git.index());
                node.accept(new SaverVisitor());
                node.setRoot(git.repo());
                node.accept(new SaverVisitor());
            }
        } else {
            lastCommitTree.setRoot(git.index());
            lastCommitTree.accept(new SaverVisitor());
            lastCommitTree.setRoot(git.repo());
            lastCommitTree.accept(new SaverVisitor());
        }
    }

    public CommandResult remove(List<Path> files) throws IOException {
        List<Path> filesInIndex = files.stream()
                .map(f -> git.index().resolve(
                        git.repo().relativize(f.toAbsolutePath())).toAbsolutePath())
                .collect(Collectors.toList());
        GitFileManager.removeAll(filesInIndex);

//        List<Path> filesInCWD = files.stream()
//                .map(git.repo()::resolve)
//                .collect(Collectors.toList());
//        GitFileManager.removeAll(filesInCWD);

        return new CommandResult(ExitStatus.SUCCESS, "rm: done!");
    }

    public CommandResult status(String revision) throws GitException, IOException {
        HeadInfo headInfo = logger.getHeadInfo();
        Message message = new Message();
        if(!headInfo.currentHash().equals(headInfo.headHash())) {
            message.write("On commit " + headInfo.currentHash() + sep);
        } else {
            message.write("On branch " + headInfo.branch() + sep);
        }

        if(headInfo.merging()) {
            if (headInfo.mergeConflictFlag()) {
                message.write("You have unmerged paths." + sep);
                message.write("Unmerged paths:" + sep);
                message.write(headInfo.getConflictingFilesAsString());
                return new CommandResult(ExitStatus.FAILURE, message);
            } else {
                message.write("All conflicts fixed but you are still merging." + sep);
            }
        }

        if ((revision == null || revision.isEmpty())) {
            String currentTreeHash = logger.currentTreeHash();

            if (currentTreeHash.isEmpty()) {
                Tree index = Tree.createTree(git.index());
                Tree repo = Tree.createTree(git.repo());
                Status repoToIndex = new Status(repo, index);
                if (index.isEmpty()  && repo.isEmpty() && !headInfo.mergeConflictFlag()) {
                    message.write("No commits yet");
                } else {
                    message.write(repoToIndex.toString());
                }
                return new CommandResult(ExitStatus.SUCCESS, message);
            }
            revision = currentTreeHash;
        }
        Optional<Path> commitFile = fileManager.findFileInStorage(revision);
        if (commitFile.isPresent()) {
            Tree index = Tree.createTree(git.index());
            Tree repo = Tree.createTree(git.repo());
            Tree lastCommit = new TreeEncoder(git.storage()).decode(commitFile.get());

            StatusInfo statusInfo = new StatusInfo(repo, lastCommit, index);
            if (statusInfo.isEmpty()) {
                message.write("No changed files");
            } else {
                message.write(statusInfo.toString());
            }
            return new CommandResult(ExitStatus.SUCCESS, message);
        }
        String failMessage = "status: '" + revision + "' unknown revision";
        return new CommandResult(ExitStatus.FAILURE, failMessage);
    }

    public CommandResult checkoutNewBranch(String branch) throws GitException, IOException {
        if (logger.getHeadInfo().mergeConflictFlag()) {
            return new CommandResult(ExitStatus.FAILURE,
                    "Unresolved conflict; fix conflicts first.");
        }
        newBranch(branch);
        checkoutBranch(branch);
        return new CommandResult(ExitStatus.SUCCESS, "On branch " + branch);
    }

    private CommandResult checkoutBranch(String branch) throws GitException, IOException {
        if (logger.getHeadInfo().mergeConflictFlag()) {
            return new CommandResult(ExitStatus.FAILURE,
                    "Unresolved conflict; fix conflicts first.");
        }
        HeadInfo headInfo = logger.getHeadInfo();
        String lastCommit = logger.getHead(branch);
        if (headInfo.branch().equals(branch) && headInfo.currentHash().equals(lastCommit)) {
            return new CommandResult(ExitStatus.FAILURE,
                        "Already on branch '" + branch + "'");
        }
        if (Files.notExists(git.log().resolve(branch))) {
            return new CommandResult(ExitStatus.FAILURE,
                    "No such branch '" + branch + "' found");
        }
        String head = logger.getHead(branch);

        if (head.isEmpty()) {
            headInfo = new HeadInfo();
            logger.writeHeadInfo(headInfo);
        } else if (head.equals(headInfo.currentHash())) {
            CommandResult res = checkout(head);
            if (res.getStatus() == ExitStatus.ERROR) {
                return res;
            }
            headInfo.moveBoth(head);
        } else {
            GitFileManager.clearDirectory(git.repo());
            restoreCommitToIndexAndRepo(head, null);
        }
        logger.changeHeadInfo(head, branch);
        return new CommandResult(ExitStatus.SUCCESS, "Switched to branch '" + branch + "'");
    }

    public CommandResult newBranch(String branch) throws GitException, IOException {
        Path logFile = git.log().resolve(branch);
        if (Files.exists(logFile)) {
            return new CommandResult(ExitStatus.FAILURE,
                    "branch with name '" + branch + "' already exists");
        }
        logger.newLogFile(logFile);
        return new CommandResult(ExitStatus.SUCCESS, "branch '" + branch + "' created");
    }

    public CommandResult deleteBranch(String branch) throws GitException, IOException {
        Path branchFile = git.log().resolve(branch);
        if (Files.notExists(branchFile)) {
            return new CommandResult(ExitStatus.FAILURE,
                    "Branch with name '" + branch + "' does not exist");
        }
        if (logger.getHeadInfo().branch().equals(branch)) {
            return new CommandResult(ExitStatus.ERROR,
                    "Cannot delete the current branch '" + branch + "'");
        }
        if (!logger.getHead(branch).isEmpty()) {
            List<CommitInfo> branchHistory = logger.getBranchCommits(branch);
            deleteCommits(branchHistory);
        }
        Files.delete(branchFile);
        return new CommandResult(ExitStatus.SUCCESS, "branch '" + branch + "' deleted");
    }

    public CommandResult listBranches() throws IOException {
        Message message = new Message();
        Files.list(git.log()).forEach(b -> message.write(b.getFileName() + sep));
        return new CommandResult(ExitStatus.SUCCESS, message);
    }

    public CommandResult merge(String incomingHash) throws IOException, GitException {
        HeadInfo headInfo = logger.getHeadInfo();
        if (headInfo.merging()) {
//            if(headInfo.mergeConflictFlag()) {
                return new CommandResult(ExitStatus.ERROR,
                        "fatal: You have not concluded your merge (MERGE_HEAD exists)." + sep +
                                "Please, commit your changes before you merge.");
//            } else {
//                CommandResult res = commit(null);
//                if(res.getStatus().equals(ExitStatus.SUCCESS)) {
//                    return  res;
//                } else {
//                    return new CommandResult(ExitStatus.SUCCESS, "merge: done!");
//                }
//            }
        }
        if (Files.exists(git.log().resolve(incomingHash))) {
            incomingHash = logger.getHead(incomingHash);
        }
        Optional<Path> commitFile = fileManager.findFileInStorage(incomingHash);
        if (commitFile.isPresent()) {
            String incomingBranch = Files.readAllLines(commitFile.get()).get(2);
            String currentHash = headInfo.currentHash();

            List<CommitInfo> branchCommits = logger.getBranchCommits(incomingBranch);
            if(branchCommits.size() > 0
                    && branchCommits.get(branchCommits.size() - 1).previousCommitHash().equals(currentHash)) {
                List<CommitInfo> history = logger.getHistory(incomingBranch);
                int difference = GitLogger.getDifference(currentHash, incomingHash, history);

                if (difference > 0) {
                    return commitFastForward(incomingBranch, incomingHash);
                } else if (difference < 0) {
                    return new CommandResult(ExitStatus.FAILURE, "Already up-to-date.");
                }
            }

            TreeEncoder encoder = new TreeEncoder(git.storage());
            Tree incomingTree = encoder.decode(incomingHash);

            SaverVisitor visitor = new SaverVisitor(true, incomingBranch);
            incomingTree.setRoot(git.repo());
            incomingTree.accept(visitor);
            if (visitor.conflictsAcquired()) {
                Message message = new Message();
                message.write("Cannot merge" + sep + "CONFLICT (content):" + sep);
                visitor.conflictingFiles().forEach(f -> message.write(tab + f + sep));
                message.write("Automatic merge failed; fix conflicts and then run merge again");

                logger.turnOnConflictingAndMerging(visitor.conflictingFiles(), incomingBranch);
                return new CommandResult(ExitStatus.FAILURE, message);
            }

            return commitMerge(incomingBranch);
        }
        return new CommandResult(ExitStatus.FAILURE,
                "No such branch or commit '" + incomingHash + "'");
    }

    private CommandResult commitFastForward(String incomingBranch, String incomingHash) throws GitException, IOException {
        String infoMessage = "fast forward merge branch '" + incomingBranch +
                "' to branch '" + logger.getHeadInfo().branch() + "'";

        TreeEncoder encoder = new TreeEncoder(git.storage());
        Tree incomingTree = encoder.decode(incomingHash);

        incomingTree.setRoot(git.index());
        incomingTree.accept(new SaverVisitor());
        incomingTree.setRoot(git.repo());
        incomingTree.accept(new SaverVisitor());

        CommandResult res = commit(infoMessage);
        if (!res.getStatus().equals(ExitStatus.SUCCESS)) {
            return res;
        }
        return new CommandResult(ExitStatus.SUCCESS, "Fast forward");
    }

    private CommandResult commitMerge(String incomingBranch) throws GitException, IOException {
        String infoMessage = "branch '" + incomingBranch +
                "' merged to branch '" +
                logger.getHeadInfo().branch() + "'";

        Tree repoTree = Tree.createTree(git.repo());
        repoTree.setRoot(git.index());
        repoTree.accept(new SaverVisitor());

        CommandResult res = commit(infoMessage);
        if (!res.getStatus().equals(ExitStatus.SUCCESS)) {
            return res;
        }
        return new CommandResult(ExitStatus.SUCCESS, "merge: done!");
    }

    private void deleteCommits(List<CommitInfo> commits) throws GitException, IOException {
        Usages usages = logger.getUsages();
        CleanerVisitor visitor = new CleanerVisitor(usages, git.storage());
        TreeEncoder encoder = new TreeEncoder(git.storage());

        for (CommitInfo c : commits) {
            Tree tree = encoder.decode(c.hash());
            tree.accept(visitor);
            File dir = fileManager.getDir(c.hash()).toFile();
            if (dir.list() == null || dir.list().length == 0) {
                Files.deleteIfExists(dir.toPath());
            }
        }
        logger.writeUsages(visitor.usages());
    }

}
