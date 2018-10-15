package ru.ifmo.git.commands;

import java.io.IOException;

import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.entities.GitTree;
import ru.ifmo.git.util.*;

public interface GitCommand {

    default boolean incorrectArgs() {
        return false;
    }

    default boolean gitNotInited(GitTree tree) {
        return !tree.exists();
    }

    CommandResult doWork(GitManager gitManager) throws GitException, IOException;

    default CommandResult execute(GitManager gitManager) {
        String name = this.getClass().getSimpleName().toLowerCase();
        if (gitNotInited(gitManager.tree())) {
            return new CommandResult(ExitStatus.ERROR, "fatal: Not a l_git repository");
        }
        if (incorrectArgs()) {
            return new CommandResult(ExitStatus.ERROR, name + ": incorrect arguments");
        }
        try {
            return doWork(gitManager);
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, name + ": " + e.getMessage());
        } catch (IOException e) {
            return new CommandResult(ExitStatus.ERROR, name + ": error appeared", e);
        }
    }

}
