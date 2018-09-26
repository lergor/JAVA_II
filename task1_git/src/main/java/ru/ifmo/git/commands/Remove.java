package ru.ifmo.git.commands;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Remove implements GitCommand {

    private List<String> files;
    private GitAssembly git;

    public Remove() {
        git = new GitAssembly(GitAssembly.cwd());
    }

    public Remove(Path cwd) {
        git = new GitAssembly(cwd);
    }

    @Override
    public boolean correctArgs(Map<String, Object> args) {
        files = ((List<String>) args.get("<pathspec>"));
        return !files.isEmpty();
    }

    @Override
    public CommandResult doWork(Map<String, Object> args) {
        if (!git.tree().exists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: not a m_git repository");
        }
        try {
            List<Path> filesInIndex = files.stream().map(git.tree().index()::resolve).collect(Collectors.toList());
            List<Path> filesInCWD = files.stream().map(git.tree().repo()::resolve).collect(Collectors.toList());
            GitFileKeeper.removeAll(filesInIndex);
            GitFileKeeper.removeAll(filesInCWD);
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, "remove: " + e.getMessage());
        }
        return new CommandResult(ExitStatus.SUCCESS, "remove: done!");
    }
}
