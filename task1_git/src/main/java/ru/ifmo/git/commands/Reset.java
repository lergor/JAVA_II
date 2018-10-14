package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.GitException;

@Command(
        name = "reset",
        description = "Reset current HEAD to the specified state",
        helpCommand = true
)
public class Reset implements GitCommand {

    @Parameters(arity = "1", paramLabel = "<revision>")
    private String revision;

    @Override
    public boolean incorrectArgs() {
        return (revision.length() < 6);
    }

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException {
        return gitManager.reset(revision);
    }
}
