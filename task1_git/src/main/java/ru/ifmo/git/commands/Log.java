package ru.ifmo.git.commands;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class Log implements GitCommand {

    private HeadInfo headInfo;
    private String commit;
    private GitTree gitTree;
    private GitClerk gitClerk;
    private GitFileKeeper gitFileKeeper;
    private GitCryptographer gitCrypto;

    public Log() {
        initEntities(GitTree.cwd());
    }

    public Log(Path cwd) {
        initEntities(cwd);
    }

    private void initEntities(Path cwd) {
        gitTree = new GitTree(cwd);
        gitClerk = new GitClerk(gitTree);
        gitFileKeeper = new GitFileKeeper(gitTree);
        gitCrypto = new GitCryptographer(gitTree);
    }

    @Override
    public boolean correctArgs(Map<String, Object> args) {
        commit = (String) args.get("<commit>");
        return commit == null || commit.length() > 6;
    }

    @Override
    public CommandResult doWork(Map<String, Object> args) throws GitException {
        if (!gitTree.exists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: not a m_git repository");
        }
        return readLog(commit);
    }


    private CommandResult readLog(String revision) {
        if (Files.exists(gitTree.log())) {
            Message logContent = new Message();
            List<CommitInfo> history;
            try {
                history = gitClerk.getLogHistory();
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
