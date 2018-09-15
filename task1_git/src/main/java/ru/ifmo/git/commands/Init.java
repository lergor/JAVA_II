package ru.ifmo.git.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import ru.ifmo.git.util.*;

import java.io.*;
import java.util.*;

public class Init implements Command {

    private File gitDirectory = null;

    @Override
    public CommandResult execute(List<String> args) {
        if(!correctArgs(args)) {
            return new CommandResult(ExitStatus.ERROR, "m_git: init: wrong arguments number\n");
        }
        gitDirectory = getGitDirectory();
        CommandResult result = new CommandResult(ExitStatus.SUCCESS);
        Message message = new Message();
        if(repositoryExists()) {
            message.write("reinitialized existing ");
        } else {
            if(initRepository()) {
                message.write("initialized empty ");
            } else {
                result.setStatus(ExitStatus.FAILURE);
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

    private boolean createDirs() {
        return
                createDir("/logs/") &&
                createDir("/storage/") &&
                createDir("/index/") &&
                createDir("/info/") &&
                createDir("/refs/heads/");
    }

    private boolean createFiles() {
        HeadInfo masterInfo = new HeadInfo("master");
        String headInfo = (new Gson()).toJson(masterInfo);
        return createFileWithContent("/HEAD", headInfo);
    }

    private boolean createDir(String dirName) {
        File newDir = new File(gitDirectory.getAbsolutePath() + dirName);
        return newDir.exists() || newDir.mkdirs();
    }

}
