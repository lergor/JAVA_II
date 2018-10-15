package ru.ifmo.git.commands;

import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.GitException;

import java.io.IOException;

@Command(
        name = "branch",
        description = "List, create, or delete branches",
        helpCommand = true
)
public class Branch implements GitCommand {
    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display more info about command 'branch'"
    )
    boolean usageHelpRequested;

    @Option(
            names = {"-n", "--new"},
            arity = "1",
            paramLabel = "<new_branch>",
            description = "Create a new branch named <new_branch>",
            type = String.class
    )
    private String branchToCreate;

    @Option(
            names = {"-d", "--delete"},
            arity = "1",
            paramLabel = "<branch>",
            description = "Delete a branch",
            type = String.class
    )
    private String branchToDelete;

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException, IOException {
        if(branchToCreate != null) {
            return gitManager.newBranch(branchToCreate);
        } else if(branchToDelete != null) {
            return gitManager.deleteBranch(branchToDelete);
        } else {
            return gitManager.listBranches();
        }
    }
}
