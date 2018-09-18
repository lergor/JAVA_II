package ru.ifmo.git.commands;

import ru.ifmo.git.masters.*;
import ru.ifmo.git.util.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

public class Log implements Command {

    private HeadInfo headInfo;
    private String commit;

    @Override
    public boolean correctArgs(Map<String, Object> args) {
        if (args.size() == 1) {
            commit = (String) args.get("<commit>");
            return true;
        }
        return (args.size() == 0);
    }

    @Override
    public CommandResult execute(Map<String, Object> args) {
        try {
            checkRepoAndArgs(args);
            headInfo = FileMaster.getHeadInfo(new File(GitTree.head()));
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, "error while reading HEAD\n");
        }
        return readLog(Paths.get(GitTree.log(), headInfo.branchName).toFile(), commit);
    }

    private CommandResult readLog(File logFile, String revision) {
        if (logFile.exists()) {
            Message logContent = new Message();
            List<CommitInfo> history;
            try {
                history = FileMaster.getHistory(logFile);
            } catch (GitException e) {
                return new CommandResult(ExitStatus.ERROR, e.getMessage());
            }
            if (history.size() == 0) {
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
