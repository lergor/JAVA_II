package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;

import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.GitException;

@Command(
        name = "reset",
        description = "Reset current HEAD to the specified state",
        helpCommand = true
)
public class Reset implements GitCommand {

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display more info about command 'reset'"
    )
    boolean usageHelpRequested;

    @Parameters(arity = "1", paramLabel = "<revision>")
    private String revision;

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException, IOException {
        return gitManager.reset(revision);
    }

}
