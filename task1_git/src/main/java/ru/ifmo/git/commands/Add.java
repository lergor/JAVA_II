package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.List;

import ru.ifmo.git.entities.GitFileKeeper;
import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.GitException;

@Command(
        name = "add",
        description = "Add file contents to the index",
        helpCommand = true
)
public class Add implements GitCommand {

    @Parameters(arity = "*", paramLabel = "<pathspec>")
    private List<Path> files;

    @Override
    public boolean incorrectArgs() {
        return !GitFileKeeper.checkFilesExist(files, true);
    }

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException {
        return gitManager.add(files);
    }
}
