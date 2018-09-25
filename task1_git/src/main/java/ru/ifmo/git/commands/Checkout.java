//package ru.ifmo.git.commands;
//
//import ru.ifmo.git.entities.*;
//import ru.ifmo.git.util.*;
//
//import java.io.*;
//import java.util.Map;
//
//import org.apache.commons.io.FileUtils;
//
//public class Checkout implements GitCommand {
//
//    private String commit;
//
//    @Override
//    public boolean correctArgs(Map<String, Object> args) {
//        commit = (String) args.get("<commit>");
//        return commit.length() > 6;
//    }
//
//    @Override
//    public CommandResult execute(Map<String, Object> args) {
//        try {
//            checkRepoAndArgs(args);
//            File commitDir = FileMaster.findCommitInStorage(commit);
//            FileUtils.copyDirectory(commitDir, new File(GitTree.cwd()));
//            FileMaster.changeCurHash(commitDir.getName(), false);
//        } catch (IOException | GitException e) {
//            return new CommandResult(ExitStatus.ERROR, e.getMessage());
//        }
//        return new CommandResult(ExitStatus.SUCCESS, "checkout: done!");
//    }
//
//}
