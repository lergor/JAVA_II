package ru.ifmo.git.commands;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

import java.nio.file.Path;
import java.util.List;

@Command(
        name = "checkout",
        description = "Switch branches or restore working tree files",
        helpCommand = true
)
public class Checkout implements GitCommand {

    @Parameters(
            arity = "?",
            paramLabel = "<revision>"
    )
    private String revision;

    @Option(
            names = {"--", "-r"},
            arity = "*",
            paramLabel = "<file>",
            description = "Discard changes in working directory in the given files.",
            type = Path.class
    )
    private List<Path> files;

    @Override
    public boolean incorrectArgs() {
        return (revision == null || revision.length() < 6) && (files.isEmpty());
    }

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException {
        if(revision != null) {
            return gitManager.checkout(revision);
        } else {
            return gitManager.checkout(files);
        }
    }
}
