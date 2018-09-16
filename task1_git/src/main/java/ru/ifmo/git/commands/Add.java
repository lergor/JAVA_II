package ru.ifmo.git.commands;

import ru.ifmo.git.masters.StorageMaster;
import ru.ifmo.git.util.*;

import java.io.*;
import java.util.*;

public class Add implements Command {

    @Override
    public boolean correctArgs(List<String> args) {
        if(args.isEmpty()) {
            return false;
        }
        for (String fileName: args) {
            File file = new File(fileName);
            if(!file.exists()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public CommandResult execute(List<String> args) {
        try {
            checkRepoAndArgs(args);
            StorageMaster.copyAll(args,  ".", ".m_git/index");
            return new CommandResult(ExitStatus.SUCCESS, "add: done!\n");
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, "add: " + e.getMessage());
        }
    }
}
