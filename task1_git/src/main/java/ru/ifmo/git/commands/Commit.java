package ru.ifmo.git.commands;

import ru.ifmo.git.util.*;

import com.google.gson.Gson;
import java.io.*;
import java.text.*;
import java.util.*;

public class Commit implements Command {

    private HeadInfo headInfo;
    private CommitInfo commitInfo = new CommitInfo();

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
            return new CommandResult(ExitStatus.ERROR, "aborting commit due to empty commit message.\n");
        }
        commitInfo.message = args.get(0);
        if(new File(commitInfo.message).exists()) {
            commitInfo.message = "";
        } else {
            args = args.subList(1, args.size());
            if(args.isEmpty()) {
                return new CommandResult(ExitStatus.ERROR, "no changes added to commit\n");
            }
        }
        if(!correctArgs(args)) {
            return new CommandResult(ExitStatus.ERROR, "fatal: did not match some files to commit\n");
        }
        try {
            headInfo = getHeadInfo();
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, e.getMessage());
        }
        setCommitInfo();
        CommandResult result = copyAllToDir(args, headInfo.storagePath);
        if(result.getStatus() == ExitStatus.ERROR) {
            return result;
        }
        return writeLog();
    }

    private void setCommitInfo() {
        commitInfo.author = System.getProperty("user.name");

        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZ");
        Calendar calendar = Calendar.getInstance();
        commitInfo.time = df.format(calendar.getTime());

        String hash = String.valueOf(commitInfo.time.concat(String.valueOf(getCWD())).hashCode());
        commitInfo.hash = (UUID.randomUUID().toString() + hash).replaceAll("-", "");

        commitInfo.branch = headInfo.branchName;

        headInfo.setHeadHash(commitInfo.hash);
    }

    private CommandResult writeLog() {
        if(commitInfo.message.isEmpty()) {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                System.out.print("please enter message: ");
                commitInfo.message = br.readLine();
            } catch (IOException e) {
                commitInfo.message = "no message";
            }
        }
        String logContent = (new Gson()).toJson(commitInfo);
        if(!writeToFile(headInfo.logFilePath, logContent, true)) {
            return new CommandResult(ExitStatus.FAILURE, "cannot write to log\n");
        }
        return new CommandResult(ExitStatus.SUCCESS, "commit: done!");
    }

}
