package ru.ifmo.git.entities;

import picocli.CommandLine;

import ru.ifmo.git.commands.GitCommand;
import ru.ifmo.git.util.*;
import sun.misc.IOUtils;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GitManager {

    private GitTree tree;
    private GitClerk clerk;
    private GitFileKeeper fileKeeper;

    private static final String sep = System.getProperty("line.separator");

    public GitManager(Path directory) {
        this.tree = new GitTree(directory);
        clerk = new GitClerk(tree);
        fileKeeper = new GitFileKeeper(tree);
    }

    public GitTree tree() {
        return tree;
    }

    public CommandResult executeCommand(CommandLine commandLine) {
        GitCommand command = commandLine.getCommand();
        return command.execute(this);
    }

    public CommandResult init() throws GitException {
        Message message = new Message();
        if (!tree.exists()) {
            try {
                tree.createGitTree();
                message.write("initialized empty ");
                clerk.writeHeadInfo(new HeadInfo());
            } catch (IOException e) {
                String msg = "unable to create repository in " + tree.repo();
                return new CommandResult(ExitStatus.FAILURE, msg, e);
            }
        } else {
            message.write("reinitialized existing ");
        }
        message.write("lGit repository in " + tree.repo());
        return new CommandResult(ExitStatus.SUCCESS, message);
    }

    public CommandResult add(List<Path> files) throws IOException {
        GitFileKeeper.copyAll(files, tree.repo(), tree.index());
        return new CommandResult(ExitStatus.SUCCESS, "add: done!");
    }

    public CommandResult commit(String message) throws GitException, IOException {
        CommitInfo commitInfo = clerk.fillCommitInfo(message);
        List<FileReference> references = GitEncoder.formCommitReferences(commitInfo.hash, tree.index());
        fileKeeper.saveCommit(references);
        clerk.writeLog(commitInfo);
        clerk.changeHeadInfo(commitInfo.hash);
        return new CommandResult(ExitStatus.SUCCESS, "commit: done!");
    }

    public CommandResult log(String revision) throws GitException {
        CommandResult emptyLog = new CommandResult(ExitStatus.SUCCESS, clerk.emptyLogResult());
        if (Files.notExists(tree.log())) {
            return emptyLog;
        }
        List<CommitInfo> history = clerk.getLogHistory();
        Collections.reverse(history);
        if (history.size() == 0) {
            return emptyLog;
        }
        history = history.stream().filter(
                new Predicate<CommitInfo>() {
                    private boolean include = (revision == null || revision.isEmpty());

                    @Override
                    public boolean test(CommitInfo commitInfo) {
                        include = include || !commitInfo.hash.startsWith(revision);
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
        if (clerk.getHeadInfo().currentHash.equals(revision)) {
            return new CommandResult(ExitStatus.FAILURE,
                    "reset: already on commit " + revision);
        }
        Optional<Path> commit = fileKeeper.findFileInStorage(revision);
        if (commit.isPresent()) {
            Path commitFile = commit.get();
            List<FileReference> references = GitDecoder.formCommitReferences(commitFile, fileKeeper);
            GitFileKeeper.clearDirectory(tree.index());
            fileKeeper.restoreCommit(references, tree.index());
            clerk.changeHeadInfo(revision);
            return new CommandResult(ExitStatus.SUCCESS, "reset: done!");
        }
        String failMessage = "reset: '" + revision + "' unknown revision";
        return new CommandResult(ExitStatus.FAILURE, failMessage);
    }

    public CommandResult checkout(String revision) throws GitException, IOException {
        if(Files.exists(tree.log().resolve(revision))) {
            return checkoutBranch(revision);
        }
        if (clerk.getHeadInfo().currentHash.equals(revision)) {
            return new CommandResult(ExitStatus.FAILURE,
                    "checkout: already on commit " + revision);
        }
        Optional<Path> commit = fileKeeper.findFileInStorage(revision);
        if (commit.isPresent()) {
            Path commitFile = commit.get();
            List<FileReference> references = GitDecoder.formCommitReferences(commitFile, fileKeeper);
            GitFileKeeper.clearDirectory(tree.index());
            fileKeeper.restoreCommit(references, tree.index());
            GitFileKeeper.copyAll(Files.list(tree.index()).collect(Collectors.toList()),
                    tree.index(), tree.repo());
            clerk.changeHeadInfo(revision);
        }
        return new CommandResult(ExitStatus.SUCCESS, "checkout: done!");
    }

    public CommandResult checkout(List<Path> files) throws IOException {
        files = files.stream()
                .map(f -> tree.index().resolve(tree.repo().relativize(f)))
                .collect(Collectors.toList());
        if (!GitFileKeeper.checkFilesExist(files)) {
            return new CommandResult(ExitStatus.FAILURE, "");
        }
        GitFileKeeper.copyAll(files, tree.index(), tree.repo());
        return new CommandResult(ExitStatus.SUCCESS, "checkout: done!");
    }

    public CommandResult status() throws GitException, IOException {
        HeadInfo headInfo = clerk.getHeadInfo();
        Message info = new Message();
        info.write("On branch " + headInfo.branchName + sep);
        Map<String, String> fileToStatus = clerk.compareRepoAndIndex();
        if (fileToStatus.isEmpty()) {
            info.write("No changed files");
        } else {
            for (Map.Entry<String, String> e : fileToStatus.entrySet()) {
                info.write(sep);
                info.write(e.getValue() + ": ");
                info.write("\t");
                info.write(e.getKey());
            }
        }
        return new CommandResult(ExitStatus.SUCCESS, info);
    }

    public CommandResult remove(List<Path> files) throws IOException {
        List<Path> filesInIndex = files.stream()
                .map(f -> tree.index().resolve(tree.repo().relativize(f)))
                .collect(Collectors.toList());
        GitFileKeeper.removeAll(filesInIndex);
//        List<Path> filesInCWD = files.stream()
//                .map(tree.repo()::resolve)
//                .collect(Collectors.toList());
//        GitFileKeeper.removeAll(filesInCWD);
        return new CommandResult(ExitStatus.SUCCESS, "rm: done!");
    }

    public CommandResult checkoutNewBranch(String branch) throws GitException, IOException {
        newBranch(branch);
        checkoutBranch(branch);
        return new CommandResult(ExitStatus.SUCCESS, "On branch " + branch);
    }

    private CommandResult checkoutBranch(String branch) throws GitException, IOException {
        HeadInfo headInfo = clerk.getHeadInfo();
        if(headInfo.branchName.equals(branch)) {
            return new CommandResult(ExitStatus.FAILURE,
                    "Already on branch '" + branch + "'");
        }

        String head = clerk.getHead(branch);
        if(head.isEmpty()) {
            headInfo = new HeadInfo();
        }
        headInfo.setBranchName(branch);
        clerk.writeHeadInfo(headInfo);

        if(!head.isEmpty()) {
            CommandResult res = checkout(head);
            if(res.getStatus() != ExitStatus.SUCCESS) {
                return res;
            }
            headInfo.moveBoth(head);
        }
        clerk.writeHeadInfo(headInfo);
        return new CommandResult(ExitStatus.SUCCESS, "On branch " + branch);
    }

    public CommandResult newBranch(String branch) throws GitException, IOException {
        Path logFile = tree.log().resolve(branch);
        if(Files.exists(logFile)) {
            return new CommandResult(ExitStatus.FAILURE,
                    "branch with name '" + branch + "' already exists");
        }
        Files.createFile(logFile);
        String parentBranch = BlobType.PARENT_BRANCH.asString() + clerk.getHeadInfo().branchName + sep;
        GitClerk.writeToFile(logFile, parentBranch, false);
        return new CommandResult(ExitStatus.SUCCESS, "On branch " + branch);
    }

    public CommandResult deleteBranch(String branch) throws GitException, IOException {
        Path branchFile = tree.log().resolve(branch);
        if(Files.notExists(branchFile)) {
            return new CommandResult(ExitStatus.FAILURE,
                    "branch with name '" + branch + "' does not exist");
        }
        if(clerk.getHeadInfo().branchName.equals(branch)) {
            return new CommandResult(ExitStatus.ERROR,
                    "cannot delete the current branch '" + branch + "'");
        }
        String head = clerk.getHead(branch);
        if(!head.isEmpty()) {
            fileKeeper.deleteCommit(head, clerk);
            for (CommitInfo c: clerk.getBranchCommits(branch)) {
                fileKeeper.deleteCommit(c.hash, clerk);
            }
        }
        Files.delete(branchFile);
        return new CommandResult(ExitStatus.SUCCESS, "Branch '" + branch + "' deleted");
    }

    public CommandResult listBranches() throws IOException {
        Message message = new Message();
        Files.list(tree.log()).forEach(b -> message.write(b.getFileName().toString() + sep));
        return new CommandResult(ExitStatus.SUCCESS, message);
    }
}
