package ru.ifmo.git.commands;

import ru.ifmo.git.util.Command;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.ExitStatus;
import ru.ifmo.git.util.Message;

import java.io.File;
import java.nio.file.*;
import java.util.List;

public class Commit implements Command {

    @Override
    public boolean correctArgs(List<String> args) {
        for(String fileName: args) {
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
        if(args.isEmpty()) {
            return new CommandResult(ExitStatus.ERROR, "Aborting commit due to empty commit message.\n");
        }
        String message = args.get(0);
        boolean needMessage = false;
        if(new File(message).exists()) {
            needMessage = true;
            args = args.subList(1, args.size());
        }
        if(!correctArgs(args)) {
            return new CommandResult(ExitStatus.ERROR, "fatal: did not match some files to commit\n");
        }

        CommandResult result = new CommandResult(ExitStatus.SUCCESS);
        String cwd = getCWD();

        return result;
    }

    private boolean checkLogDir() {
        return false;
    }
}
