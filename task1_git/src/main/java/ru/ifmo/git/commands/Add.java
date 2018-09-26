package ru.ifmo.git.commands;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Add implements GitCommand {

    private List<Path> files;
    private GitAssembly git;

    public Add() {
        git = new GitAssembly(GitAssembly.cwd());
    }

    public Add(Path cwd) {
        git = new GitAssembly(cwd);
    }

    private List<Path> getArgs(Map<String, Object> args) {
        return ((List<String>) args.get("<pathspec>"))
                .stream()
                .map(s -> Paths.get(s).normalize())
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
    public CommandResult doWork(Map<String, Object> args) throws GitException {
        if (!git.tree().exists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: not a m_git repository");
        }
        try {
            GitFileKeeper.copyAll(files, git.tree().index());
        } catch (IOException e) {
            throw new GitException(e.getMessage());
        }
        return new CommandResult(ExitStatus.SUCCESS, "add: done!");
    }
}
