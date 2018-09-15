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
    public CommandResult execute(List<String> args) throws GitException {
        if(!repositoryExists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: Not a git repository: .m_git\n");
        }
        if(!correctArgs(args)) {
            return new CommandResult(ExitStatus.ERROR, "fatal: did not match some files\n");
        }
        return moveAllToIndex(args);
    }

    private CommandResult moveAllToIndex(List<String> args) {
        File index = new File(getGitPath() + "/index/");
        if(index.exists() || (!index.exists() && index.mkdirs())) {
            for (String fileName: args) {
                if(fileName.startsWith("./")) {
                    fileName = fileName.substring(1, fileName.length());
                }
                try {
                    copyToIndex(fileName);
                } catch (GitException e) {
                    return new CommandResult(ExitStatus.ERROR, e.getMessage());
                }
            }
        }
        return new CommandResult(ExitStatus.SUCCESS, "added.\n");

    }

    private void copyToIndex(String source) throws GitException {
        File sourceFile = new File(getCWD() + source);
        File destinationFile = new File(getGitPath() + "/index/" + source);
        if(!sourceFile.isHidden()) {
            try {
                if(sourceFile.isFile()) {
                    FileUtils.copyFileToDirectory(sourceFile, destinationFile);
                } else if(sourceFile.isDirectory()) {
                    File[] files = sourceFile.listFiles(pathname -> !pathname.isHidden());
                    if(files != null) {
                        for (File file: files) {
                            String shortPath = "/" + sourceFile.getName() + "/" + file.getName();
                            copyToIndex(shortPath);
                        }
                    }
                }
            } catch (IOException e) {
                throw new GitException("error while moving " + source + "\n");
            }
        }
    }
}
