package ru.ifmo.git;

import picocli.CommandLine;
//import ru.ifmo.git.entities.GitParser;
import ru.ifmo.git.commands.Git;
//import ru.ifmo.git.commands1.Init;
import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LGit {

    private static Path cwd() {
        //        System.out.println(System.getProperty("user.dir"));
        return Paths.get("/home/valeriya/Desktop/AU/III/JAVA_II/task1_git/kek");
    }

    public static void main(String[] args) {
        Path repo = Paths.get("/home/valeriya/Desktop/AU/III/JAVA_II/task1_git/kek");
        args = new String[]{"add",
                repo.resolve("lol").toString()};

//        args = new String[]{"checkout", "780228fc7a09bf494f5922c6e310cf36244317c9"};
//        args = new String[]{"checkout", "-r", repo.resolve("lol").toString()};
        args = new String[]{"-h"};
        CommandLine commandLine = new CommandLine(new Git());
        try {
            List<CommandLine> parsed = commandLine.parse(args);

            // TODO git help

            if (parsed.size() == 2) {
                GitManager manager = new GitManager(cwd());
                CommandResult result = manager.executeCommand(parsed.get(1));
                if (result.getStatus() != ExitStatus.SUCCESS) {
                    System.out.println("Exit with code " + result.getStatus());
                }
                System.out.println(result.getMessage().read());
            } else {
                commandLine.usage(System.out);
            }
        } catch (CommandLine.UnmatchedArgumentException e) {
            System.out.println("l_git: no such command. See 'l_git --help'.");
        }
    }
}
