package ru.ifmo.git.commands;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class Reset implements GitCommand {

    private Path commitFile;
    private GitAssembly git;

    public Reset() {
        git = new GitAssembly(GitAssembly.cwd());
    }

    public Reset(Path cwd) {
        git = new GitAssembly(cwd);
    }

    @Override
    public boolean correctArgs(Map<String, Object> args) throws GitException {
        String commitHash = (String) args.get("<commit>");
        if (commitHash.length() > 6) {
            try {
                Optional<Path> commit = git.fileKeeper().findFileInStorage(commitHash);
                if (commit.isPresent()) {
                    commitFile = commit.get();
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new GitException(e.getMessage());
            }
        }
        return false;
    }

    @Override
    public CommandResult doWork(Map<String, Object> args) throws GitException {
        if (!git.tree().exists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: not a m_git repository");
        }
        try {
            List<FileReference> references = git.crypto().formDecodeReferences(commitFile);
            GitFileKeeper.clearDirectory(git.tree().index());
            git.fileKeeper().restoreCommit(references, git.tree().index());
            changeHeadInfo();
        } catch (IOException | GitException e) {
            return new CommandResult(ExitStatus.ERROR, e.getMessage());
        }
        return new CommandResult(ExitStatus.SUCCESS, "reset: done!");

    }

    private void changeHeadInfo() throws GitException {
        HeadInfo headInfo = git.clerk().getHeadInfo();
        String hash = commitFile.getParent().toFile().getName() + commitFile.toFile().getName();
        if (!headInfo.currentHash.equals(hash)) {
            headInfo.moveCurrent(hash);
            git.clerk().changeHeadInfo(headInfo);
        } else {
            throw new GitException("reset: already on commit " + hash);
        }
    }
}
