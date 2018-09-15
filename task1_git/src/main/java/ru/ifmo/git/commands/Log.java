package ru.ifmo.git.commands;

import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import ru.ifmo.git.util.*;

import java.io.*;
import java.util.List;

public class Log implements Command {

    @Override
    public boolean correctArgs(List<String> args) {
        return args.size() <= 1;
    }

    @Override
    public CommandResult execute(List<String> args) {
        if(!repositoryExists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: Not a git repository: .m_git\n");
        }
        if(!correctArgs(args)) {
            return new CommandResult(ExitStatus.ERROR, "log: too many arguments\n");
        }
        String logPath;
        try {
            logPath = getHeadInfo().logFilePath;
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, "error while reading HEAD\n");
        }
        return readLog(new File(getGitPath() + "/" + logPath));
    }

    private CommandResult readLog(File logFile) {
        if(logFile.exists()) {
            Message logContent = new Message();
            try(BufferedReader br = new BufferedReader(new FileReader(logFile))) {
                for(String line; (line = br.readLine()) != null; ) {
                    logContent.write(getCommitInfo(line + "\n"));
                }
            } catch (IOException e) {
                return new CommandResult(ExitStatus.ERROR, "error while reading log\n");
            }
            return new CommandResult(ExitStatus.SUCCESS, logContent);
        }
        return new CommandResult(ExitStatus.FAILURE, "fatal: your current branch 'master' does not have any commits yet\n");
    }

    private String getCommitInfo(String commitJson) {
        CommitInfo commitInfo = new GsonBuilder().create().fromJson(commitJson, CommitInfo.class);
        return  "commit " + commitInfo.hash + "\n" +
                "Author:\t" + commitInfo.author + "\n" +
                "Date:\t" + commitInfo.time + "\n" +
                "\t\t" + commitInfo.message + "\n\n";
    }

}
