package ru.ifmo.git.commands;

import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.ExitStatus;
import ru.ifmo.git.util.GitException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface GitCommand {

    boolean correctArgs(Map<String, Object> args) throws GitException;

    CommandResult doWork(Map<String, Object> args) throws GitException;

    default CommandResult execute(Map<String, Object> args) {
        String name = this.getClass().getSimpleName().toLowerCase();
        try {
            if (correctArgs(args)) {
                return doWork(args);
            }
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, name + ": " + e.getMessage());
        }
        return new CommandResult(ExitStatus.ERROR, name + ": wrong args");

    }

    default void checkFilesExist(List<Path> files) throws GitException {
        for (Path file : files) {
            if (!Files.exists(file)) {
                String message = "fatal: pathspec " + file.getFileName() + " did not match any files";
                throw new GitException(message);
            }
        }
    }

}
