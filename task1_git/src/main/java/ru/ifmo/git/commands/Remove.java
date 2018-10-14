package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

import java.nio.file.Path;
import java.util.*;

@Command(
        name = "rm",
        description = "Remove files from the working tree and from the index",
        helpCommand = true
)
public class Remove implements GitCommand {

    @Parameters(arity = "*", paramLabel = "<pathspec>")
    private List<Path> files;

    @Override
    public boolean incorrectArgs() {
        return files.isEmpty();
    }

    @Override
    public CommandResult doWork(GitManager gitManager) {
        return gitManager.remove(files);
    }
}
