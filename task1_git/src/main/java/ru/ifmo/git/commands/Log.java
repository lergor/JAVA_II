package ru.ifmo.git.commands;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class Log implements GitCommand {

    private String commit;
    private GitAssembly git;

    public Log() {
        git = new GitAssembly(GitAssembly.cwd());
    }

    public Log(Path cwd) {
        git = new GitAssembly(cwd);
    }

    @Override
    public boolean correctArgs(Map<String, Object> args) {
        commit = (String) args.get("<commit>");
        return commit == null || commit.length() > 6;
    }

    @Override
    public CommandResult doWork(Map<String, Object> args) throws GitException {
        if (!git.tree().exists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: not a m_git repository");
        }
        return readLog(commit);
    }


    private CommandResult readLog(String revision) throws GitException {
        if (Files.exists(git.tree().log())) {
            Message logContent = new Message();
            List<CommitInfo> history;
            try {
                history = git.clerk().getLogHistory();
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

    private CommandResult emptyLogResult() throws GitException {
        HeadInfo headInfo = git.clerk().getHeadInfo();
        String failMessage = "fatal: your current branch '" + headInfo.branchName + "' does not have any commits yet\n";
        return new CommandResult(ExitStatus.FAILURE, failMessage);
    }
}
