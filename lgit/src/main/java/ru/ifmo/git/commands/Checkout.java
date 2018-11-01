package ru.ifmo.git.commands;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.io.IOException;

import java.nio.file.Path;

import java.util.List;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

@Command(
        name = "checkout",
        description = "Switch branches or restore working tree files",
        helpCommand = true
)
public class Checkout implements GitCommand {

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display more info about command 'checkout'"
    )
    boolean usageHelpRequested;

    @Parameters(
            arity = "?",
            paramLabel = "<revision>/<branch>",
            description = "Switch to revision or branch by updating the index and " +
                    "the files in the working tree, and by pointing HEAD at the branch"
    )
    private String revision;

    @Option(
            names = {"-r"},
            arity = "*",
            paramLabel = "<file>",
            description = "Discard changes in working directory in the given files",
            type = Path.class
    )
    private List<Path> files;

    @Option(
            names = {"-b"},
            arity = "1",
            paramLabel = "<new_branch>",
            description = "Create a new branch named <new_branch> and checkout to it",
            type = String.class
    )
    private String branchName;

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException, IOException {
        if (revision != null) {
            return gitManager.checkout(revision);
        } else if (files != null) {
            return gitManager.checkout(files);
        } else {
            return gitManager.checkoutNewBranch(branchName);
        }
    }

}
