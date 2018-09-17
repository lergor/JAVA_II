package ru.ifmo.git.commands;

import ru.ifmo.git.masters.*;
import ru.ifmo.git.util.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import com.google.gson.Gson;

public class Init implements Command {

    private File gitDirectory = null;

    @Override
    public boolean correctArgs(Map<String, Object> args) {
        return args == null || args.size() == 0;
    }

    @Override
    public CommandResult execute(Map<String, Object> args) {
        if (!correctArgs(args)) {
            return new CommandResult(ExitStatus.ERROR, "init: wrong arguments\n");
        }
        gitDirectory = Paths.get(GitTree.repo()).toFile();
        CommandResult result = new CommandResult(ExitStatus.SUCCESS);
        Message message = new Message();
        if (repositoryExists()) {
            message.write("reinitialized existing ");
        } else {
            if (initRepository()) {
                message.write("initialized empty ");
            } else {
                result.setStatus(ExitStatus.ERROR);
                message.write("fail to init ");
            }
        }
        message.write("Git repository in " + gitDirectory.getAbsolutePath() + "\n");
        result.setMessage(message);
        return result;
    }

    private boolean initRepository() {
        return gitDirectory.mkdir() && createDirs() && createFiles();
    }

    private boolean createDir(String dirName) {
        File newDir = Paths.get(GitTree.repo(), dirName).toFile();
        return newDir.exists() || newDir.mkdirs();
    }

    private boolean createDirs() {
        return
                createDir("/logs/") &&
                        createDir("/storage/") &&
                        createDir("/index/");
    }

    private boolean createFiles() {
        HeadInfo masterInfo = new HeadInfo("master");
        String headInfo = (new Gson()).toJson(masterInfo);
        return StorageMaster.createFileWithContent(GitTree.head(), headInfo);
    }

}
