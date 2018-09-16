package ru.ifmo.git.commands;

import ru.ifmo.git.masters.StorageMaster;
import ru.ifmo.git.util.*;

import java.io.File;
import java.util.List;

public class Checkout implements Command {

    @Override
    public boolean correctArgs(List<String> args) {
        return args.size() == 1 && args.get(0).length() > 6;
    }

    @Override
    public CommandResult execute(List<String> args) {
        try {
            checkRepoAndArgs(args);
            File commitDir = GitUtils.findCommitInStorage(args.get(0));
            String storagePath = ".m_git/storage/" + commitDir.getName();
            StorageMaster.copyDirToDir(storagePath, ".");
            GitUtils.changeCurHash(commitDir.getName(), true);
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, e.getMessage());
        }
        return new CommandResult(ExitStatus.SUCCESS, "checkout: done!");
    }
}
