package ru.ifmo.git.entities;

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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GitManager {

    private GitStructure git;
    private GitFileManager fileManager;
    private GitLogger logger;

    private static final String sep = System.getProperty("line.separator");

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
        if (logger.getHeadInfo().mergeConflictFlag()) {
            return new CommandResult(ExitStatus.FAILURE,
                    "Unresolved conflict; fix conflicts first.");
        }
        Tree repository = Tree.createTree(git.repo());
        repository.setRoot(git.index());
        repository.accept(new SaverVisitor());

        Tree index = Tree.createTree(git.index());
        String currentTreeHash = logger.currentTreeHash();
        if (currentTreeHash != null && !currentTreeHash.isEmpty()) {
            Tree lastCommit = new TreeEncoder(git.storage()).decode(currentTreeHash);
            StatusInfo statusInfo = new StatusInfo(repository, lastCommit, index);

            files = files.stream()
                    .map(f -> git.index().resolve(git.repo().relativize(f)))
                    .collect(Collectors.toList());
            for (Tree tree : Tree.createTrees(files, git.index())) {
                if (statusInfo.getDeleted(false).contains(tree.path())) {
                    tree.accept(new DeleteVisitor());
                }
            }
        }
        return new CommandResult(ExitStatus.SUCCESS, "add: done!");
    }

    public CommandResult commit(String message) throws GitException, IOException {
        if (logger.getHeadInfo().mergeConflictFlag()) {
            return new CommandResult(ExitStatus.FAILURE,
                    "Unresolved conflict; fix conflicts first.");
        }
        CommitVisitor visitor = new CommitVisitor();
        Tree tree = Tree.createTree(git.index());
        String currentHash = logger.currentTreeHash();

        if (tree.isEmpty() || !currentHash.isEmpty() && currentHash.equals(tree.hash())) {
            return new CommandResult(ExitStatus.SUCCESS,
                    "Nothing to commit, working tree clean");
        }
        tree.accept(visitor);

        CommitInfo commitInfo = logger.fillCommitInfo(message);
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
        if (logger.getHeadInfo().currentHash().isEmpty()) {
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
        return logger.getHistory().stream().filter( new Predicate<CommitInfo>() {
            private boolean include = true;

            @Override
            public boolean test(CommitInfo commitInfo) {
                if (commitInfo.hash.equals(revision)) {
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
        Tree lastCommitTree = new TreeEncoder(git.storage()).decode(lastCommit);
        List<Tree> nodes = new ArrayList<>();
        for (Path f : files) {
            String file = git.repo().relativize(f).toString();
            Tree node = lastCommitTree.find(file);
            if (node == null) {
                return new CommandResult(ExitStatus.FAILURE, "No such file '" + file + "' found");
            }
            node.setRoot(git.index());
            node.accept(new SaverVisitor());
            node.setRoot(git.repo());
            node.accept(new SaverVisitor());

        }
        return successResult;
    }

    public CommandResult remove(List<Path> files) throws IOException {
        List<Path> filesInIndex = files.stream()
                .map(f -> git.index().resolve(git.repo().relativize(f)))
                .collect(Collectors.toList());
        GitFileManager.removeAll(filesInIndex);

        List<Path> filesInCWD = files.stream()
                .map(git.repo()::resolve)
                .collect(Collectors.toList());
        GitFileManager.removeAll(filesInCWD);

        return new CommandResult(ExitStatus.SUCCESS, "rm: done!");
    }

    public CommandResult status(String revision) throws GitException, IOException {
        HeadInfo headInfo = logger.getHeadInfo();
        Message info = new Message();
        info.write("On branch " + headInfo.branch() + sep);

        if ((revision == null || revision.isEmpty())) {
            String currentTreeHash = logger.currentTreeHash();
            if (currentTreeHash.isEmpty()) {
                List<String> indexFiles = Files.list(git.index())
                        .map(f -> git.index().relativize(f).toString())
                        .collect(Collectors.toList());
                if (indexFiles.isEmpty()) {
                    info.write("No commits yet");
                } else {
                    indexFiles.forEach(f -> info.write("new : " + f + sep));
                }
                return new CommandResult(ExitStatus.SUCCESS, info);
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
                info.write("No changed files");
            } else {
                info.write(sep);
                info.write(statusInfo.toString());
            }
            return new CommandResult(ExitStatus.SUCCESS, info);
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
        if (headInfo.branch().equals(branch)) {
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
        if (headInfo.mergeConflictFlag()) {
            if (logger.stillConflicting(headInfo.getConflictingFiles())) {
                return new CommandResult(ExitStatus.FAILURE,
                        "Unresolved conflict; fix conflicts first.");
            }
            logger.turnOffConflicting();
            return commitMerge(headInfo.mergeBranch());
        }
        if (Files.exists(git.log().resolve(incomingHash))) {
            incomingHash = logger.getHead(incomingHash);
        }
        Optional<Path> commitFile = fileManager.findFileInStorage(incomingHash);
        if (commitFile.isPresent()) {
            String incomingBranch = Files.readAllLines(commitFile.get()).get(2);
            String currentHash = logger.currentTreeHash();

            List<CommitInfo> history = logger.getHistory(incomingBranch);
            int difference = GitLogger.getDifference(currentHash, incomingHash, history);
            if (difference > 0) {
                CommandResult res = checkout(incomingHash);
                if (res.getStatus() != ExitStatus.SUCCESS) {
                    return res;
                }
                logger.changeHeadInfo(incomingHash);
                return new CommandResult(ExitStatus.SUCCESS, "Fast forward");
            } else if (difference < 0) {
                return new CommandResult(ExitStatus.FAILURE, "Already up-to-date.");
            }

            TreeEncoder encoder = new TreeEncoder(git.storage());
            Tree incomingTree = encoder.decode(incomingHash);

            SaverVisitor visitor = new SaverVisitor(true);
            incomingTree.setRoot(git.repo());
            incomingTree.accept(visitor);
            if (visitor.conflictsAcquired()) {
                Message message = new Message();
                message.write("Cannot merge" + sep + "Conflicts:" + sep);
                visitor.conflictingFiles().forEach(f -> message.write(f + sep));
                message.write("Automatic merge failed; fix conflicts and then run merge again");

                logger.turnOnConflicting(visitor.conflictingFiles(), incomingBranch);
                return new CommandResult(ExitStatus.FAILURE, message);
            }

            return commitMerge(incomingBranch);
        }
        return new CommandResult(ExitStatus.FAILURE,
                "No such branch or commit '" + incomingHash + "'");
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
