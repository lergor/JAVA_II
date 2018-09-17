package ru.ifmo.git.commands;

import ru.ifmo.git.masters.*;
import ru.ifmo.git.util.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class Add implements Command {

    private List<File> files = new LinkedList<>();

    @Override
    public boolean correctArgs(Map<String, Object> args) {
        if (args.isEmpty()) {
            return false;
        }
        //noinspection unchecked
        List<String> fileNames = ((List<String>) args.get("<pathspec>"));
        for (String fileName : fileNames) {
            File file = Paths.get(GitTree.cwd(), fileName).toFile();
            if (!file.exists()) {
                return false;
            }
            files.add(file);
        }
        return true;
    }

    @Override
    public CommandResult execute(Map<String, Object> args) {
        try {
            checkRepoAndArgs(args);
            StorageMaster.copyAll(files, new File(GitTree.index()));
            return new CommandResult(ExitStatus.SUCCESS, "add: done!\n");
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, "add: " + e.getMessage());
        }
    }

}
