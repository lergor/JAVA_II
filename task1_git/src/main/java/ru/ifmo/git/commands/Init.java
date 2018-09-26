package ru.ifmo.git.commands;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

public class Init implements GitCommand {

    private GitAssembly git;

    @Override
    public boolean correctArgs(Map<String, Object> args) {
        return args.size() == 1 && Files.exists((Path) args.get("<directory>"));
    }

    @Override
    public CommandResult doWork(Map<String, Object> args) throws GitException {
        git = new GitAssembly((Path) args.get("<directory>"));
        Message message = new Message();
        if (!git.tree().exists()) {
            try {
                git.tree().createGitTree();
                message.write("initialized empty ");
                writeHead();
            } catch (IOException e) {
                String msg = "unable to create repository in " + git.tree().repo();
                return new CommandResult(ExitStatus.FAILURE, msg);
            }
        } else {
            message.write("reinitialized existing ");
        }
        message.write("Git repository in " + git.tree().repo());
        return new CommandResult(ExitStatus.SUCCESS, message);
    }

    private void writeHead() throws GitException {
        HeadInfo info = new HeadInfo();
        info.branchName = "master";
        GitClerk clerk = new GitClerk(git.tree());
        clerk.changeHeadInfo(info);
    }
}
