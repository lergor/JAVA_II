package ru.ifmo.git.commands;

import ru.ifmo.git.masters.*;
import ru.ifmo.git.util.*;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Paths;
import java.text.*;
import java.util.*;

public class Commit implements Command {

    private HeadInfo headInfo;
    private CommitInfo commitInfo = new CommitInfo();
    private List<File> files = new LinkedList<>();
    private String message;

    @Override
    public boolean correctArgs(Map<String, Object> args) {
        //noinspection unchecked
        List<String> fileNames = ((List<String>) args.get("<pathspec>"));
        for (String fileName : fileNames) {
            File file = Paths.get(GitTree.index(), fileName).toFile();
            if (!file.exists()) {
                return false;
            }
            files.add(file);
        }
        message = (String) args.get("message");
        return true;
    }

    @Override
    public CommandResult execute(Map<String, Object> args) {
        try {
            checkRepoAndArgs(args);
            headInfo = FileMaster.getHeadInfo();
            setCommitInfo();
            StorageMaster.copyAll(files, Paths.get(GitTree.storage(), headInfo.currentHash).toFile());
            writeLog();
            return new CommandResult(ExitStatus.SUCCESS, "commit: done!\n");
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, "commit: " + e.getMessage());
        }
    }


    private void setCommitInfo() throws GitException {
        commitInfo.author = System.getProperty("user.name");
        commitInfo.message = message;
        commitInfo.branch = headInfo.branchName;

        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZ");
        Calendar calendar = Calendar.getInstance();
        commitInfo.time = df.format(calendar.getTime());

        String hash = String.valueOf(commitInfo.time.concat(String.valueOf(GitTree.cwd())).hashCode());
        commitInfo.hash = (UUID.randomUUID().toString() + hash).replaceAll("-", "");

        boolean moveHead = headInfo.headHash.equals(headInfo.currentHash);
        FileMaster.changeCurHash(commitInfo.hash, moveHead);
    }

    private void writeLog() throws GitException {
        if (commitInfo.message.isEmpty()) {
            getUserMessage();
        }
        String logContent = (new Gson()).toJson(commitInfo);
        String logFile = Paths.get(GitTree.log(), headInfo.branchName).toString();
        FileMaster.writeToFile(logFile, logContent, true);
    }

    private void getUserMessage() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("please enter message: ");
            commitInfo.message = br.readLine();
        } catch (IOException e) {
            commitInfo.message = "no message";
        }
    }

}
