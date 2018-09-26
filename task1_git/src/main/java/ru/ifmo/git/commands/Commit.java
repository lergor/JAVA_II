package ru.ifmo.git.commands;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Commit implements GitCommand {

    private CommitInfo commitInfo;
    private GitAssembly git;

    public Commit() {
        git = new GitAssembly(GitAssembly.cwd());
    }

    public Commit(Path cwd) {
        git = new GitAssembly(cwd);
    }

    @Override
    public boolean correctArgs(Map<String, Object> args) {
        return true;
    }

    @Override
    public CommandResult doWork(Map<String, Object> args) {
        if (!git.tree().exists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: not a m_git repository");
        }
        try {
            List<Path> files = Files.list(git.tree().index()).collect(Collectors.toList());
            commitInfo = git.clerk().fillCommitInfo((String) args.getOrDefault("message", ""));
            List<FileReference> references = git.crypto().formEncodeReferences(files);
            FileReference commitReference = git.crypto().formHeaderReference(commitInfo.hash, files);
            references.add(commitReference);
            git.fileKeeper().saveCommit(references);
            git.clerk().writeLog(commitInfo);
            changeHeadInfo();
        } catch (IOException | GitException e) {
            return new CommandResult(ExitStatus.ERROR, e.getMessage());
        }
        return new CommandResult(ExitStatus.SUCCESS, "commit: done!");
    }

    private void changeHeadInfo() throws GitException {
        HeadInfo headInfo = git.clerk().getHeadInfo();
        if (headInfo.headHash == null || headInfo.headHash.equals(headInfo.currentHash)) {
            headInfo.moveBoth(commitInfo.hash);
        } else {
            headInfo.moveCurrent(commitInfo.hash);
        }
        git.clerk().changeHeadInfo(headInfo);
    }
}
