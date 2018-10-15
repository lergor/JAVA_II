package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.GitException;

@Command(
        name = "log",
        description = "Show commit logs",
        helpCommand = true
)
public class Log implements GitCommand {

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display more info about command log."
    )
    boolean usageHelpRequested;

    @Parameters(
            arity = "?",
            paramLabel = "<revision>",
            description = "Show only commits since specified <revision>."
    )
    private String revision;

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException {
        return gitManager.log(revision);
    }

}
