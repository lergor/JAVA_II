package ru.ifmo.git.commands;

import ru.ifmo.git.entities.GitFileKeeper;
import ru.ifmo.git.entities.GitTree;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.ExitStatus;
import ru.ifmo.git.util.GitException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Remove implements GitCommand {

    private GitTree gitTree;
    List<String> files;

    public Remove() {
        gitTree = new GitTree(GitTree.cwd());
    }

    public Remove(Path cwd) {
        gitTree = new GitTree(cwd);
    }

    @Override
    public boolean correctArgs(Map<String, Object> args) {
        files = ((List<String>) args.get("<pathspec>"));
        return !files.isEmpty();
    }

    @Override
    public CommandResult doWork(Map<String, Object> args) {
        if (!gitTree.exists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: not a m_git repository");
        }
        try {
            List<Path> filesInIndex = files.stream().map(gitTree.index()::resolve).collect(Collectors.toList());
            List<Path> filesInCWD = files.stream().map(gitTree.repo()::resolve).collect(Collectors.toList());
            GitFileKeeper.removeAll(filesInIndex);
            GitFileKeeper.removeAll(filesInCWD);
        } catch (IOException e) {
            return new CommandResult(ExitStatus.ERROR, "remove: " + e.getMessage());
        }
        return new CommandResult(ExitStatus.SUCCESS, "remove: done!\n");
    }
}
