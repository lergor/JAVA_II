package ru.ifmo.git.commands;

import ru.ifmo.git.masters.StorageMaster;
import ru.ifmo.git.util.*;
import com.google.gson.Gson;

import java.io.*;
import java.text.*;
import java.util.*;

public class Commit implements Command {

    private HeadInfo headInfo;
    private CommitInfo commitInfo = new CommitInfo();
    List<String> arguments;

    @Override
    public boolean correctArgs(List<String> args) {
        for(String fileName: args) {
            File file = new File(GitUtils.getGitPath() + "/index/" + fileName);
            if(!file.exists()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public CommandResult execute(List<String> args) {
        try {
            arguments = args;
            checkUserInput();
            checkRepoAndArgs(arguments);
            headInfo = GitUtils.getHeadInfo();
            setCommitInfo();
            StorageMaster.copyAll(arguments,  ".m_git/index", ".m_git/storage/" + headInfo.currentHash);
            writeLog();
            GitUtils.changeCurHash(commitInfo.hash, false);
            return new CommandResult(ExitStatus.SUCCESS, "commit: done!\n");
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, "commit: " + e.getMessage());
        }
    }

    private void checkUserInput() throws GitException {
        if(arguments.isEmpty()) {
            throw new GitException("aborting commit due to empty commit message\n");
        }
        commitInfo.message = arguments.get(0);
        if(new File(commitInfo.message).exists()) {
            commitInfo.message = "";
        } else {
            arguments = arguments.subList(1, arguments.size());
            if(arguments.isEmpty()) {
                throw new GitException("no changes added to commit\n");
            }
        }
        if(!correctArgs(arguments)) {
            throw new GitException("did not match some files to commit\n");
        }
    }

    private void setCommitInfo() {
        commitInfo.author = System.getProperty("user.name");

        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZ");
        Calendar calendar = Calendar.getInstance();
        commitInfo.time = df.format(calendar.getTime());

        String hash = String.valueOf(commitInfo.time.concat(String.valueOf(GitUtils.getCWD())).hashCode());
        commitInfo.hash = (UUID.randomUUID().toString() + hash).replaceAll("-", "");

        commitInfo.branch = headInfo.branchName;

        headInfo.setCurrentHash(commitInfo.hash);
    }

    private void writeLog() throws GitException {
        if(commitInfo.message.isEmpty()) {
            getUserMessage();
        }
        String logContent = (new Gson()).toJson(commitInfo);
        GitUtils.writeToFile(GitUtils.getGitPath() + "/" + headInfo.logFilePath, logContent, true);
    }

    private void getUserMessage() {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("please enter message: ");
            commitInfo.message = br.readLine();
        } catch (IOException e) {
            commitInfo.message = "no message";
        }
    }
}
