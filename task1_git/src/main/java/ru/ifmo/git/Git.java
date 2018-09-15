package ru.ifmo.git;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

//import net.sourceforge.argparse4j.ArgumentParsers;
//import net.sourceforge.argparse4j.inf.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import ru.ifmo.git.commands.Add;
import ru.ifmo.git.commands.Init;
import ru.ifmo.git.util.BranchInfo;
import ru.ifmo.git.util.Command;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.GitException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Git {

    private File gitDirectory;

    private void start_session() {
        String cwd = Paths.get(".").toAbsolutePath().normalize().toString();
        gitDirectory = new File(cwd + "/.m_git");
    }

//    static private ArgumentParser createParser() {
//        ArgumentParser gitParser = ArgumentParsers.newArgumentParser("git");
//        gitParser.defaultHelp(true)
//                .description("A version control system created by lergor.");
//
//        Subparsers subparsers = gitParser.addSubparsers();
//
//        Subparser parserInit = subparsers.addParser("init")
//                .help("Create an empty Git repository or reinitialize an existing one");
//
//        Subparser parserAdd = subparsers.addParser("add")
//                .help("Add file contents to the index");
//        parserAdd.addArgument("file").type(String.class)
//                .required(true).nargs("+");
//
//        Subparser parserCommit = subparsers.addParser("commit")
//                .help("Record changes to the repository");
//        parserCommit.addArgument("message").type(String.class).required(true);
//        parserCommit.addArgument("file").type(String.class).nargs("+");
//
//        Subparser parserReset = subparsers.addParser("reset")
//                .help("Reset current HEAD to the specified state");
//        parserReset.addArgument("<commit>").type(String.class).required(true);
//
//        Subparser parserLog = subparsers.addParser("log")
//                .help("Show commit logs");
//        parserLog.addArgument("<commit>").type(String.class).nargs("?");
//
//        Subparser parserCheckout = subparsers.addParser("checkout")
//                .help("Switch branches or restore working tree files");
//        parserCheckout.addArgument("<branch>").type(String.class).nargs(1);
//
//        return gitParser;
//    }
//
//    private static void parseArguments(ArgumentParser gitParser, String[] args) {
//        Namespace ns = null;
//        try {
//            ns = gitParser.parseArgs(args);
//        } catch (ArgumentParserException e) {
//            gitParser.handleError(e);
//            System.exit(1);
//        }
////        MessageDigest digest = null;
////        try {
////            digest = MessageDigest.getInstance(ns.getString("type"));
////        } catch (NoSuchAlgorithmException e) {
////            System.err.printf("Could not get instance of algorithm %s: %s",
////                    ns.getString("type"), e.getMessage());
////            System.exit(1);
////        }
////        for (String name : ns.<String> getList("file")) {
////            Path path = Paths.get(name);
////            try (ByteChannel channel = Files.newByteChannel(path,
////                    StandardOpenOption.READ);) {
////                ByteBuffer buffer = ByteBuffer.allocate(4096);
////                while (channel.read(buffer) > 0) {
////                    buffer.flip();
////                    digest.update(buffer);
////                    buffer.clear();
////                }
////            } catch (IOException e) {
////                System.err
////                        .printf("%s: failed to read data: %s", e.getMessage());
////                continue;
////            }
////            byte md[] = digest.digest();
////            StringBuffer sb = new StringBuffer();
////            for (int i = 0, len = md.length; i < len; ++i) {
////                String x = Integer.toHexString(0xff & md[i]);
////                if (x.length() == 1) {
////                    sb.append("0");
////                }
////                sb.append(x);
////            }
////            System.out.printf("%s  %s\n", sb.toString(), name);
////        }
//    }

    public static void main(String[] args) {
//        BranchInfo k  = new BranchInfo("master", "812932983");
//        System.out.println(new GsonBuilder().create().toJson(k));
        List<String> arguments = Collections.emptyList();
        CommandResult res = null;
        arguments = Arrays.asList("./kek", "lol", "ckkc");
        System.out.println(arguments.subList(1, arguments.size()).toString());

//        Init init = new Init();
//        res = init.execute(Collections.emptyList());

//        Add add = new Add();
//        res = null;
//        try {
//            res = add.execute(arguments);
//        } catch (GitException e) {
//            System.out.println(e.getMessage());
//        }
//        System.out.print(res.getMessage().read());

    }


}
