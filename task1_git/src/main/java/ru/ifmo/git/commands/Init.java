package ru.ifmo.git.commands;

import ru.ifmo.git.entities.GitClerk;
import ru.ifmo.git.entities.GitFileKeeper;
import ru.ifmo.git.entities.GitTree;
import ru.ifmo.git.util.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Init implements GitCommand {

    private GitTree gitTree;

    public Init() {
        gitTree = new GitTree();
    }

    public Init(Path cwd) {
        gitTree = new GitTree(cwd);
    }


    @Override
    public boolean correctArgs(Map<String, Object> args) {
        return args.size() == 1 && Files.exists((Path) args.get("<directory>"));
    }

    @Override
    public CommandResult doWork(Map<String, Object> args) throws GitException {
        gitTree = new GitTree((Path) args.get("<directory>"));
        Message message = new Message();
        if (!gitTree.exists()) {
            try {
                gitTree.createGitTree();
                message.write("initialized empty ");
                writeHead();
            } catch (IOException e) {
                return new CommandResult(ExitStatus.FAILURE, "unable to create repository in " + gitTree.repo());
            }
        } else {
            message.write("reinitialized existing ");
        }
        message.write("Git repository in " + gitTree.repo() + "\n");
        return new CommandResult(ExitStatus.SUCCESS, message);
    }

    private void writeHead() throws GitException {
        HeadInfo info = new HeadInfo();
        info.branchName = "master";
        GitClerk clerk = new GitClerk(gitTree);
        clerk.changeHeadInfo(info);
    }
}
