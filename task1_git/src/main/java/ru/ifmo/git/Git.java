package ru.ifmo.git;

import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import ru.ifmo.git.commands.*;
import ru.ifmo.git.entities.GitParser;
import ru.ifmo.git.util.*;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Git {

    public static void main(String[] args) {

        args = new String[]{"init"};
        GitParser gitParser = new GitParser();
        try {
            Namespace ns = gitParser.parseArgs(args);
//            GitCommand command = (GitCommand) ns.getAttrs().remove("cmd");
//            CommandResult result = command.execute(ns.getAttrs());
//            CommandResult result = command.execute(ns.getAttrs());
            GitCommand command = new Checkout(Paths.get("./clear"));
            Map<String, Object> arrrgs = new LinkedHashMap<>();
            arrrgs.put("<commit>", Collections.singletonList("c0bbb1a5f61c8c457fed673fd"));


            CommandResult result = command.execute(arrrgs);
            if(result.getStatus() != ExitStatus.SUCCESS) {
                System.out.println("exit with code " + result.getStatus());
            }
            System.out.print(result.getMessage().read());
        } catch (ArgumentParserException e) {
            gitParser.handleError(e);
        }
    }

}
