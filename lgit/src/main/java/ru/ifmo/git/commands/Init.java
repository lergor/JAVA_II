package ru.ifmo.git.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.entities.GitStructure;
import ru.ifmo.git.util.*;

@Command(
        name = "init",
        description = "Create an empty Git repository or reinitialize an existing one",
        helpCommand = true
)
public class Init implements GitCommand {

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display more info about command 'init'"
    )
    boolean usageHelpRequested;

    @Parameters(arity = "?", paramLabel = "directory")
    private Path repositoryDirectory;

    private Path repositoryDirectory() {
        if (repositoryDirectory == null) {
            return Paths.get(System.getProperty("user.dir")).normalize().toAbsolutePath();
        }
        return repositoryDirectory;
    }

    @Override
    public boolean gitNotInited(GitStructure tree) {
        return false;
    }

    @Override
    public CommandResult doWork(GitManager gitManager) throws GitException {
        return new GitManager(repositoryDirectory()).init();
    }

}
