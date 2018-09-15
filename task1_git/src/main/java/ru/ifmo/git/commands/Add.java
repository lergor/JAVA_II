package ru.ifmo.git.commands;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import ru.ifmo.git.Git;
import ru.ifmo.git.util.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
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
        if(!repositoryExists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: Not a git repository: .m_git\n");
        }
        if(!correctArgs(args)) {
            return new CommandResult(ExitStatus.ERROR, "fatal: did not match some files\n");
        }
        CommandResult result = copyAllToDir(args, "index");
        if(result.getStatus() == ExitStatus.ERROR) {
            return result;
        }
        return new CommandResult(ExitStatus.SUCCESS, "add: done!\n");
    }
}
