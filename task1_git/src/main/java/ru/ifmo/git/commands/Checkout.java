package ru.ifmo.git.commands;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Checkout implements GitCommand {

    private Path commitFile;
    private GitAssembly git;

    public Checkout() {
        git = new GitAssembly(GitAssembly.cwd());
    }

    public Checkout(Path cwd) {
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
            GitFileKeeper.clearDirectory(git.tree().repo());
            git.fileKeeper().restoreCommit(references, git.tree().repo());
            changeHeadInfo();
        } catch (IOException e) {
            throw new GitException(e.getMessage());
        }
        return new CommandResult(ExitStatus.SUCCESS, "checkout: done!");

    }

    private void changeHeadInfo() throws GitException {
        HeadInfo headInfo = git.clerk().getHeadInfo();
        headInfo.moveCurrent(commitFile.getParent().toFile().getName() + commitFile.toFile().getName());
        git.clerk().changeHeadInfo(headInfo);
    }
}
