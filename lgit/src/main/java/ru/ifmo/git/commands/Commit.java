package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.GitException;

@Command(
        name = "commit",
        description = "Record changes to the repository",
        helpCommand = true
)
public class Commit implements GitCommand {

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display more info about command 'commit'"
    )
    boolean usageHelpRequested;

    @Option(
            names = {"-m", "--message"},
            arity = "?",
            paramLabel = "<msg>",
            description = "Use the given <msg> as the commit message"
    )
    private String message;

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException, IOException {
        return gitManager.commit(message);
    }

}
