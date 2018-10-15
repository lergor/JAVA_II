package ru.ifmo.git.commands;

import picocli.CommandLine.Command;

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

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException, IOException {
        return gitManager.status();
    }

}
