package ru.ifmo.git;

import ru.ifmo.git.masters.GitParser;
import ru.ifmo.git.util.*;

import net.sourceforge.argparse4j.inf.*;

public class Git {

    public static void main(String[] args) {
        GitParser gitParser = new GitParser();
        try {
            Namespace ns = gitParser.parseArgs(args);
            Command command = (Command) ns.getAttrs().remove("cmd");
            CommandResult result = command.execute(ns.getAttrs());
            if(result.getStatus() != ExitStatus.SUCCESS) {
                System.out.println("exit with code " + result.getStatus());
            }
            System.out.print(result.getMessage().read());
        } catch (ArgumentParserException e) {
            gitParser.handleError(e);
        } catch (GitException e) {
            System.out.print(e.getMessage());
        }
    }

}
