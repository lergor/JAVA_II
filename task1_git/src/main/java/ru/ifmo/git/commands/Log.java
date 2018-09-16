package ru.ifmo.git.commands;

import ru.ifmo.git.util.*;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;


public class Log implements Command {

    private HeadInfo headInfo;

    @Override
    public boolean correctArgs(List<String> args) {
        return args.size() == 0 || (args.size() == 1 && args.get(0).length() > 6);
    }

    @Override
    public CommandResult execute(List<String> args) {
        String logPath;
        try {
            checkRepoAndArgs(args);
            headInfo = GitUtils.getHeadInfo();
            logPath = headInfo.logFilePath;
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, "error while reading HEAD\n");
        }
        String revisionToStart = "";
        if(args.size() == 1) {
            revisionToStart = args.get(0);
        }
        return readLog(new File(GitUtils.getGitPath() + "/" + logPath), revisionToStart);
    }

    private CommandResult readLog(File logFile, String revision) {
        if(logFile.exists()) {
            Message logContent = new Message();
            List<CommitInfo> history;
            try {
                history = GitUtils.getHistory(logFile);
            } catch (GitException e) {
                return new CommandResult(ExitStatus.ERROR, e.getMessage());
            }
            if(history.size() == 0) {
                return emptyLogResult();
            }
            history.stream().filter(
                    new Predicate<CommitInfo>() {

                        private boolean include = (revision == null || revision.isEmpty());

                        @Override
                        public boolean test(CommitInfo commitInfo) {
                            include = include || commitInfo.hash.startsWith(revision);
                            return include;
                        }
                    }
            ).forEach(info -> logContent.write(info.toString()));
            return new CommandResult(ExitStatus.SUCCESS, logContent);
        }
        return emptyLogResult();

    }

    private CommandResult emptyLogResult() {
        String failMessage = "fatal: your current branch '" + headInfo.branchName + "' does not have any commits yet\n";
        return new CommandResult(ExitStatus.FAILURE, failMessage);
    }

}
