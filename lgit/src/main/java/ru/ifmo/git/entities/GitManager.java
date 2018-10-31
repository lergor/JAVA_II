package ru.ifmo.git.entities;

import picocli.CommandLine;

import ru.ifmo.git.commands.GitCommand;
import ru.ifmo.git.structs.*;
import ru.ifmo.git.tree.Tree;
import ru.ifmo.git.tree.TreeEncoder;
import ru.ifmo.git.util.*;
import ru.ifmo.git.tree.visitors.CleanerVisitor;
import ru.ifmo.git.tree.visitors.CommitVisitor;
import ru.ifmo.git.tree.visitors.DeleteVisitor;
import ru.ifmo.git.tree.visitors.SaverVisitor;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GitManager {

    private GitStructure git;
    private GitFileManager fileKeeper;
    private GitLogger logger;

    private static final String sep = System.getProperty("line.separator");

    public GitManager(Path directory) {
        git = new GitStructure(directory);
        fileKeeper = new GitFileManager(git);
        logger = new GitLogger(git);
    }

    public GitStructure structure() {
        return git;
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
        Tree repository = Tree.createTree(git.repo());
        Tree index = Tree.createTree(git.index());
        Tree lastCommit = new TreeEncoder(git.storage()).decode(logger.currentTreeHash());
        StatusInfo statusInfo = new StatusInfo(repository, lastCommit, index);

        repository.setRoot(git.index());
        repository.accept(new SaverVisitor());

        files = files.stream()
                .map(f -> git.index().resolve(git.repo().relativize(f)))
                .collect(Collectors.toList());
        for (Tree tree : Tree.createTrees(files, git.index())) {
            if (statusInfo.getDeleted(false).contains(tree.path())) {
                tree.accept(new DeleteVisitor());
            }
        }
        return new CommandResult(ExitStatus.SUCCESS, "add: done!");
    }

    public CommandResult commit(String message) throws GitException, IOException {
        CommitVisitor visitor = new CommitVisitor();
        Tree tree = Tree.createTree(git.index());
        String currentHash = logger.currentTreeHash();
        if (!currentHash.isEmpty() && currentHash.equals(tree.hash())) {
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
        List<CommitInfo> history = logger.getHistory();
        if (history.size() == 0) {
            return new CommandResult(ExitStatus.SUCCESS, logger.emptyLogResult());
        }

        history = history.stream().filter(
                new Predicate<CommitInfo>() {
                    private boolean include = (revision == null || revision.isEmpty());

                    @Override
                    public boolean test(CommitInfo commitInfo) {
                        include = include || !commitInfo.hash.equals(revision);
                        return include;
                    }
                }
        ).collect(Collectors.toList());

        if (history.size() == 0) {
            String failMessage = "log: '" + revision + "' unknown revision";
            return new CommandResult(ExitStatus.FAILURE, failMessage);
        }
        Message logContent = new Message();
        history.forEach(info -> logContent.write(info.toString()));
        return new CommandResult(ExitStatus.SUCCESS, logContent);
    }

    public CommandResult reset(String revision) throws GitException, IOException {
        if (logger.getHeadInfo().currentHash().equals(revision)) {
            return new CommandResult(ExitStatus.FAILURE,
                    "reset: already on commit " + revision);
        }
        String failMessage = "reset: '" + revision + "' unknown revision";

        Optional<Path> commit = fileKeeper.findFileInStorage(revision);
        if (commit.isPresent()) {
            Path commitFile = commit.get();
            TreeEncoder encoder = new TreeEncoder(git.storage());
            Tree tree = encoder.decode(commitFile);

            if (!tree.type().equals(BlobType.COMMIT)) {
                return new CommandResult(ExitStatus.FAILURE, failMessage);
            }

            tree.setRoot(git.index());
            tree.accept(new SaverVisitor());
            logger.changeHeadInfo(revision);

            return new CommandResult(ExitStatus.SUCCESS, "reset: done!");
        }
        return new CommandResult(ExitStatus.FAILURE, failMessage);
    }

    public CommandResult checkout(String revision) throws GitException, IOException {
        if (Files.exists(git.log().resolve(revision))) {
            return checkoutBranch(revision);
        }
        if (logger.getHeadInfo().currentHash().equals(revision)) {
            return new CommandResult(ExitStatus.FAILURE,
                    "checkout: already on commit " + revision);
        }
        String failMessage = "checkout: '" + revision + "' unknown revision";

        Optional<Path> commit = fileKeeper.findFileInStorage(revision);
        if (commit.isPresent()) {
            Path commitFile = commit.get();
            TreeEncoder encoder = new TreeEncoder(git.storage());
            Tree tree = encoder.decode(commitFile);
            if (!tree.type().equals(BlobType.COMMIT)) {
                return new CommandResult(ExitStatus.FAILURE, failMessage);
            }
            tree.setRoot(git.index());
            tree.accept(new SaverVisitor());

            GitFileManager.clearDirectory(git.repo());

            tree = Tree.createTree(git.index());
            tree.setRoot(git.repo());
            tree.accept(new SaverVisitor());

            String branch = Files.readAllLines(commitFile).get(2);
            logger.changeHeadInfo(revision, branch);
            return new CommandResult(ExitStatus.SUCCESS, "checkout: done!");
        }
        return new CommandResult(ExitStatus.FAILURE, failMessage);
    }

    public CommandResult checkout(List<Path> files) throws IOException {
        Tree index = Tree.createTree(git.index());
        List<Tree> nodes = new ArrayList<>();
        for (Path f : files) {
            String file = git.repo().relativize(f).toString();
            Tree node = index.find(file);
            if (node == null) {
                return new CommandResult(ExitStatus.FAILURE, "No such file '" + file + "' found");
            }
            node.setRoot(git.repo());
            nodes.add(node);
        }
        for (Tree node : nodes) {
            node.accept(new SaverVisitor());
        }
        return new CommandResult(ExitStatus.SUCCESS, "checkout: done!");
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

        if (revision == null || revision.isEmpty()) {
            revision = logger.currentTreeHash();
        }
        Optional<Path> commitFile = fileKeeper.findFileInStorage(revision);
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
        newBranch(branch);
        checkoutBranch(branch);
        return new CommandResult(ExitStatus.SUCCESS, "On branch " + branch);
    }

    private CommandResult checkoutBranch(String branch) throws GitException, IOException {
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
        } else if (head.equals(headInfo.currentHash())) {
            CommandResult res = checkout(head);
            if (res.getStatus() == ExitStatus.ERROR) {
                return res;
            }
            headInfo.moveBoth(head);
        }
        headInfo.setBranchName(branch);
        logger.writeHeadInfo(headInfo);
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
            Usages usages = logger.getUsages();
            List<CommitInfo> branchHistory = logger.getBranchCommits(branch);
            CleanerVisitor visitor = new CleanerVisitor(usages, git.storage());
            TreeEncoder encoder = new TreeEncoder(git.storage());

            for (CommitInfo c : branchHistory) {
                Tree tree = encoder.decode(c.hash);
                tree.accept(visitor);
                logger.writeUsages(visitor.usages());
            }
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
        if (Files.exists(git.log().resolve(incomingHash))) {
            incomingHash = logger.getHead(incomingHash);
        }
        Optional<Path> commitFile = fileKeeper.findFileInStorage(incomingHash);
        if (commitFile.isPresent()) {
            String incomingBranch = Files.readAllLines(commitFile.get()).get(2);
            String currentHash = logger.currentTreeHash();

            List<CommitInfo> history = logger.getHistory(incomingBranch);
            int difference = checkFastForward(currentHash, incomingHash, history);

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
            Tree currentTree = encoder.decode(currentHash);
            Tree incomingTree = encoder.decode(incomingHash);

            Status status = new Status(incomingTree, currentTree);
            if (status.isEmpty()) {
                checkout(incomingHash);
                return new CommandResult(ExitStatus.SUCCESS, "merge: done!");
            } else if (!status.getModifiedFiles().isEmpty()) {
                Message message = new Message();
                message.write(status.newFilesToString());
                message.write(status.deletedFilesToString());
                message.write("Cannot merge" + sep);
                message.write("CONFLICT (content):" + sep);
                message.write(status.modifiedFilesToString());
                message.write("Automatic merge failed; fix conflicts and then commit the result.");
                return new CommandResult(ExitStatus.ERROR, message);
            } else {
                incomingTree.setRoot(git.index());
                incomingTree.accept(new SaverVisitor());
                incomingTree.setRoot(git.repo());
                incomingTree.accept(new SaverVisitor());
                String infoMessage = "branch '" + incomingBranch +
                        "' merged to branch '" +
                        logger.getHeadInfo().branch() + "'";
                CommandResult res = commit(infoMessage);
                if (!res.getStatus().equals(ExitStatus.SUCCESS)) {
                    return res;
                }
                return new CommandResult(ExitStatus.SUCCESS, "merge: done!");
            }

        }
        return new CommandResult(ExitStatus.FAILURE,
                "No such branch or commit '" + incomingHash + "'");
    }

    private int checkFastForward(String currentHash, String incomingHash, List<CommitInfo> history) {
        int incoming = -1;
        int current = -1;
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).hash.equals(incomingHash)) {
                incoming = i;
            } else if (history.get(i).hash.equals(currentHash)) {
                current = i;
            }
        }
        return (incoming != -1 && current != -1) ? incoming - current : 0;
    }
}
