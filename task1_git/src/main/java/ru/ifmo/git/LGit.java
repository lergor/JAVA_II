package ru.ifmo.git;

import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

import ru.ifmo.git.commands.Git;
import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;

public class LGit {

    private static Path cwd() {
//        return Paths.get(System.getProperty("user.dir"));
        return Paths.get("/home/valeriya/Desktop/AU/III/JAVA_II/task1_git/kek");
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Git());
        try {
            List<CommandLine> parsed = commandLine.parse(args);

            // TODO git help

            if (parsed.size() == 2) {
                GitManager manager = new GitManager(cwd());
                CommandResult result = manager.executeCommand(parsed.get(1));
                result.print();
            } else {
                commandLine.usage(System.out);
            }
        } catch (CommandLine.UnmatchedArgumentException e) {
            System.out.println("l_git: no such command. See 'l_git --help'.");
        }
    }

}
