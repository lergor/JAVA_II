package ru.ifmo.git;

import net.sourceforge.argparse4j.inf.*;
import ru.ifmo.git.commands.*;
import ru.ifmo.git.entities.GitParser;
import ru.ifmo.git.util.*;


public class Git {

    public static void main(String[] args) {
        GitParser gitParser = new GitParser();
        try {
            Namespace ns = gitParser.parseArgs(args);
            GitCommand command = (GitCommand) ns.getAttrs().remove("cmd");
            CommandResult result = command.execute(ns.getAttrs());
            if(result.getStatus() != ExitStatus.SUCCESS) {
                System.out.println("exit with code " + result.getStatus());
            }
            System.out.println(result.getMessage().read());
        } catch (ArgumentParserException e) {
            gitParser.handleError(e);
        }
    }
}
