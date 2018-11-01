package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.GitException;

import java.io.IOException;

@Command(
        name = "merge",
        description = "Join two or more development histories together",
        helpCommand = true
)
public class Merge implements GitCommand {

    @Parameters(
            arity = "1",
            paramLabel = "<revision>/<branch>",
            description = "Commit, usually other branch head, or branch to merge into current branch"
    )
    private String commit;

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException, IOException {
        return gitManager.merge(commit);
    }

}
