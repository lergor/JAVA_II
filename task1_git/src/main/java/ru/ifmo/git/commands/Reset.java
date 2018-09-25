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
//public class Reset implements GitCommand {
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
//            FileUtils.copyDirectory(commitDir, new File(GitTree.index()));
//            FileMaster.changeCurHash(commitDir.getName(), true);
//        } catch (IOException | GitException e) {
//            return new CommandResult(ExitStatus.ERROR, e.getMessage());
//        }
//        return new CommandResult(ExitStatus.SUCCESS, "reset: done!");
//    }
//
//}
