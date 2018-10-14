package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

@Command(
        name = "status",
        description = "Show current state of the repository",
        helpCommand = true
)
public class Status implements GitCommand {

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException {
        return gitManager.status();
    }
}
