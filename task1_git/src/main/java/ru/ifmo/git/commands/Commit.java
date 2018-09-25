package ru.ifmo.git.commands;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.*;
import java.util.*;
import java.util.stream.Collectors;

public class Commit implements GitCommand {

    private CommitInfo commitInfo;
    private List<Path> files = new LinkedList<>();
    private GitTree gitTree;
    private GitClerk gitClerk;
    private GitFileKeeper gitFileKeeper;
    private GitCryptographer gitCrypto;

    public Commit() {
        initEntities(GitTree.cwd());
    }

    public Commit(Path cwd) {
        initEntities(cwd);
    }

    private void initEntities(Path cwd) {
        gitTree = new GitTree(cwd);
        gitClerk = new GitClerk(gitTree);
        gitFileKeeper = new GitFileKeeper(gitTree);
        gitCrypto = new GitCryptographer(gitTree);
    }

    private List<Path> getArgs(Map<String, Object> args) {
        return ((List<String>) args.get("<pathspec>"))
                .stream()
                .map(s -> gitTree.index().resolve(s).normalize())
                .collect(Collectors.toList());
    }

    @Override
    public boolean correctArgs(Map<String, Object> args) throws GitException {
        if (!args.isEmpty()) {
            List<Path> files = getArgs(args);
            checkFilesExist(files);
            this.files = files;
            return true;
        }
        return false;
    }

    @Override
    public CommandResult doWork(Map<String, Object> args) {
        if (!gitTree.exists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: not a m_git repository");
        }
        try {
            commitInfo = gitClerk.fillCommitInfo((String) args.getOrDefault("message", ""));
            List<FileReference> references = gitCrypto.formEncodeReferences(files);
            FileReference commitReference = gitCrypto.formHeaderReference(commitInfo.hash, files);
            references.add(commitReference);
            gitFileKeeper.saveCommit(references);
            gitClerk.writeLog(commitInfo);
            changeHeadInfo();
            GitFileKeeper.clearDirectory(gitTree.index());
        } catch (IOException | GitException e) {
            return new CommandResult(ExitStatus.ERROR, e.getMessage());
        }
        return new CommandResult(ExitStatus.SUCCESS, "commit: done!");
    }

    private void changeHeadInfo() throws GitException {
        HeadInfo headInfo = gitClerk.getHeadInfo();
        if (headInfo.headHash == null || headInfo.headHash.equals(headInfo.currentHash)) {
            headInfo.moveBoth(commitInfo.hash);
        } else {
            headInfo.moveCurrent(commitInfo.hash);
        }
        gitClerk.changeHeadInfo(headInfo);
    }

}
