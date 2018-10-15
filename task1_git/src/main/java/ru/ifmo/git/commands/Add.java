package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;

import java.nio.file.Path;

import java.util.List;

import ru.ifmo.git.entities.GitFileKeeper;
import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;

@Command(
        name = "add",
        description = "Add file contents to the index",
        helpCommand = true
)
public class Add implements GitCommand {

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display more info about command add."
    )
    boolean usageHelpRequested;

    @Parameters(arity = "*", paramLabel = "<pathspec>")
    private List<Path> files;

    @Override
    public boolean incorrectArgs() {
        return !GitFileKeeper.checkFilesExist(files);
    }

    @Override
    public CommandResult doWork(GitManager gitManager) throws IOException {
        return gitManager.add(files);
    }

}
