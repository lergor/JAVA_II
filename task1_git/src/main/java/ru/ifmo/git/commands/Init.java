package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.entities.GitTree;
import ru.ifmo.git.util.*;

@Command(
        name = "init",
        description = "Create an empty Git repository or reinitialize an existing one",
        helpCommand = true
)
public class Init implements GitCommand {

    @Parameters(arity = "?", paramLabel = "directory")
    private Path repositoryDirectory;

    private Path repositoryDirectory() {
        if (repositoryDirectory == null) {
            return Paths.get(System.getProperty("user.dir")).normalize().toAbsolutePath();
        }
        return repositoryDirectory;
    }

    @Override
    public boolean gitNotInited(GitTree tree) {
        return false;
    }

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException {
        return new GitManager(repositoryDirectory()).init();
    }

}
