package ru.ifmo.git.commands;

import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.GitException;

@Command(
        name = "status",
        description = "Show current state of the repository",
        helpCommand = true
)
public class Status implements GitCommand {

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display more info about command 'checkout'"
    )
    boolean usageHelpRequested;

    @Parameters(arity = "1", paramLabel = "<revision>")
    private String revision;

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException, IOException {
        return gitManager.status(revision);
    }

}
