package ru.ifmo.git.entities;

import com.google.gson.*;
import org.apache.commons.io.*;

import ru.ifmo.git.util.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class GitClerk {

    private final String ENCODING = "UTF-8";
    private Gson gson = new GsonBuilder().create();
    private final GitTree gitTree;


    public GitClerk(GitTree gitTree){
        this.gitTree = gitTree;
    }

    public HeadInfo getHeadInfo() throws GitException {
        String headJson;
        try {
            headJson = FileUtils.readFileToString(gitTree.head().toFile());
        } catch (IOException e) {
            throw new GitException("error while reading HEAD");
        }
        return gson.fromJson(headJson, HeadInfo.class);
    }

    public void changeHeadInfo(HeadInfo newHeadInfo) throws IOException {
        String newInfo = gson.toJson(newHeadInfo);
        FileUtils.writeStringToFile(gitTree.head().toFile(), newInfo, ENCODING);
    }

    public void writeLog(CommitInfo commit) throws GitException {
        if (commit.message.isEmpty()) {
            commit.message = getUserMessage();
        }
        File logFile = gitTree.log().resolve(commit.branch).toFile();
        try {
            if(logFile.exists() || logFile.createNewFile()) {
                writeToFile(logFile.toPath(), gson.toJson(commit), true);
            }
        } catch (IOException e) {
            throw new GitException(e.getMessage());
        }
    }

    private String getUserMessage() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("please enter message: ");
            return br.readLine();
        } catch (IOException e) {
            return "no message";
        }
    }

    static public void writeToFile(Path file, String content, boolean append) throws GitException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile(), append))) {
            writer.write(content);
        } catch (IOException e) {
            throw new GitException("error while writing to " + file.getFileName());
        }
    }

    public List<CommitInfo> getHistory() throws GitException {
        String branch = getHeadInfo().branchName;
        File logFile = gitTree.log().resolve(branch).toFile();
        List<CommitInfo> history = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            for (String line; (line = br.readLine()) != null; ) {
                history.add(gson.fromJson(line, CommitInfo.class));
            }
        } catch (IOException e) {
            throw new GitException("error while reading log for " + branch);
        }
        return history;
    }

    public Optional<CommitInfo> findCommitInfo(String desiredCommit) throws GitException {
        for (CommitInfo commit: getHistory()) {
            if(commit.hash.startsWith(desiredCommit)) {
                return Optional.of(commit);
            }
        }
        return Optional.empty();
    }

    public static String getAuthor() {
        return System.getProperty("user.name");
    }

    public static String getCommitMessage() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("please enter message: ");
            return br.readLine();
        } catch (IOException e) {
            return "no message";
        }
    }

    private static String getCurrentTime() {
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZ");
        return df.format(Calendar.getInstance().getTime());
    }


    public CommitInfo fillCommitInfo(String message) throws GitException {
        CommitInfo info = new CommitInfo();
        info.setAuthor(getAuthor());
        info.setTime(getCurrentTime());
        info.setRootDirectory(gitTree.repo());
        info.setMessage(message.isEmpty() ? getUserMessage() : message);
        info.setHash(GitCryptographer.createCommitHash(info));
        info.setBranch(getHeadInfo().branchName);
        return info;
    }

    public CommitInfo fillAddInfo() throws GitException {
        return fillCommitInfo("add");
    }

    public Map<String, String> collectFilesInfo(Path encodedFile) throws IOException {
        Map<String, String> nameToHash = new HashMap<>();
        List<String> lines = Files.readAllLines(encodedFile, StandardCharsets.UTF_8);
        lines.remove(0);
        for (String line: lines) {
            String[] nameAndHash = line.split("\t");
            nameToHash.put(nameAndHash[0], nameAndHash[1]);
        }
        return nameToHash;
    }

}
