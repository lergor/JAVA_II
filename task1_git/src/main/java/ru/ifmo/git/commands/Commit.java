package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

@Command(
        name = "commit",
        description = "Record changes to the repository",
        helpCommand = true
)
public class Commit implements GitCommand {

    @Option(
            names = {"-m", "--message"},
            arity = "?",
            paramLabel = "<msg>",
            description = "Use the given <msg> as the commit message."
    )
    private String message;

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException {
        return gitManager.commit(message);
    }
}
