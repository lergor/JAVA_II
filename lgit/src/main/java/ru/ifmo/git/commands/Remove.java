package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;

import java.nio.file.Path;

import java.util.*;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

@Command(
        name = "rm",
        description = "Remove files from the working tree and from the index",
        helpCommand = true
)
public class Remove implements GitCommand {

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display more info about command 'rm'"
    )
    boolean usageHelpRequested;

    @Parameters(arity = "*", paramLabel = "<pathspec>")
    private List<Path> files;

    @Override
    public boolean incorrectArgs() {
        return files.isEmpty();
    }

    @Override
    public CommandResult doWork(GitManager gitManager) throws IOException {
        return gitManager.remove(files);
    }

}
