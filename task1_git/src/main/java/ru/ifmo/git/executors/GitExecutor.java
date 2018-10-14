package ru.ifmo.git.executors;

import ru.ifmo.git.commands.GitCommand;
import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;

public interface GitExecutor {
    CommandResult execute(GitManager manager, GitCommand command);
}
