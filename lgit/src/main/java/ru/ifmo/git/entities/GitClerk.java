package ru.ifmo.git.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;

import java.io.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.*;
import java.util.stream.Collectors;

import ru.ifmo.git.util.BlobType;
import ru.ifmo.git.util.CommitInfo;
import ru.ifmo.git.util.GitException;
import ru.ifmo.git.util.HeadInfo;

public class GitClerk {

    private static final String ENCODING = "UTF-8";
    private static final String sep = System.getProperty("line.separator");
    private final Gson gson = new GsonBuilder().create();
    private final GitTree gitTree;

    GitClerk(GitTree gitTree) {
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

    void writeHeadInfo(HeadInfo newHeadInfo) throws GitException {
        String newInfo = gson.toJson(newHeadInfo);
        try {
            FileUtils.writeStringToFile(gitTree.head().toFile(), newInfo, ENCODING);
        } catch (IOException e) {
            throw new GitException("error while writing log " + e.getMessage());
        }
    }

    void changeHeadInfo(String hash) throws GitException {
        HeadInfo headInfo = getHeadInfo();
        if (headInfo.headHash == null || headInfo.headHash.equals(headInfo.currentHash)) {
            headInfo.moveBoth(hash);
        } else {
            headInfo.moveCurrent(hash);
        }
        writeHeadInfo(headInfo);
    }

    public void writeLog(CommitInfo commit) throws GitException {
        File logFile = gitTree.log().resolve(commit.branch).toFile();
        try {
            if (logFile.exists() || logFile.createNewFile()) {
                writeToFile(logFile.toPath(), gson.toJson(commit) + sep, true);
            }
        } catch (IOException e) {
            throw new GitException("error while creating log " + e.getMessage());
        }
    }

    private String getUserMessage() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("please enter message: ");
            return br.readLine();
        } catch (IOException e) {
            return "";
        }
    }

    static void writeToFile(Path file, String content, boolean append) throws GitException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile(), append))) {
            writer.write(content);
        } catch (IOException e) {
            throw new GitException("error while writing to " + file.getFileName());
        }
    }

    public List<CommitInfo> getLogHistory(String branch) throws GitException {
        File logFile = gitTree.log().resolve(branch).toFile();
        List<CommitInfo> history = new ArrayList<>();
        if(!logFile.exists()) {
            return history;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            for (String line; (line = br.readLine()) != null; ) {
                if(line.startsWith(BlobType.PARENT_BRANCH.asString())) {
                    history.addAll(getLogHistory(line.substring(BlobType.size())));
                } else {
                    history.add(gson.fromJson(line, CommitInfo.class));
                }
            }
        } catch (IOException e) {
            throw new GitException("error while reading log for " + branch);
        }
        return history;
    }

    public List<CommitInfo> getLogHistory() throws GitException {
        return getLogHistory(getHeadInfo().branchName);
    }

    private static String getAuthor() {
        return System.getProperty("user.name");
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
        info.setMessage(message == null ? getUserMessage() : message);
        info.setHash(GitEncoder.createCommitHash(info));
        info.setBranch(getHeadInfo().branchName);
        return info;
    }

    String emptyLogResult() throws GitException {
        HeadInfo headInfo = getHeadInfo();
        return "fatal: your current branch '" +
                headInfo.branchName +
                "' does not have any commits yet\n";
    }

    public Map<String, String> compareRepoAndIndex() throws GitException, IOException {
        Map<String, String> fileToStatus = new HashMap<>();
        List<String> inIndex = collectNames(gitTree.index());
        List<String> inCWD = collectNames(gitTree.repo());
        for (String file : inIndex) {
            Path fileInCWD = gitTree.repo().resolve(file);
            Path fileInIndex = gitTree.index().resolve(file);
            if (Files.isRegularFile(fileInIndex)) {
                if (!Files.exists(fileInCWD)) {
                    fileToStatus.put(file, "deleted");
                } else {
                    String hashInIndex = GitEncoder.getHash(fileInIndex, gitTree.index());
                    String hashInCWD = GitEncoder.getHash(fileInCWD, gitTree.repo());
                    if (!hashInCWD.equals(hashInIndex)) {
                        fileToStatus.put(file, "modified");
                    }
                }
            }
        }
        inCWD.forEach(f -> {
            if(!f.contains(".l_git") && !Files.exists(gitTree.index().resolve(f))) {
                fileToStatus.put(f, "new");
            }
        });
        return fileToStatus;
    }

    List<String> collectNames(Path directory) throws IOException {
        List<String> fileNames = new ArrayList<>();
        List<Path> files = Files.list(directory).collect(Collectors.toList());
        for (Path file : files) {
            String fileName = file.toFile().getName();
            if (!fileName.equals(".l_git")) {
                fileNames.add(fileName);
                if (Files.isDirectory(file)) {
                    List<String> fromSubDir = collectNames(file);
                    for (String f : fromSubDir) {
                        fileNames.add(Paths.get(fileName, f).toString());
                    }
                }
            }
        }
        return fileNames;
    }

    String getHead(String branch) throws GitException {
        List<CommitInfo> history = getLogHistory(branch);
        if(history.size() == 0) {
            return "";
        }
        return history.get(history.size() - 1).hash;
    }

    public Map<String, String> collectEncodedFiles(Path encodedTree, GitFileKeeper storage) throws IOException {
        Map<String, String> hashToName = new HashMap<>();
        List<String> lines = Files.readAllLines(encodedTree, Charset.forName(ENCODING));
        lines.remove(0);
        for (String line: lines) {
            BlobType type = GitDecoder.readMarker(line);
            String[] hashAndName = line.substring(BlobType.size()).split("\t");
            if(type == BlobType.FILE) {
                hashToName.put(hashAndName[1], hashAndName[0]);
            } else {
                hashToName.putAll(collectEncodedFiles(storage.correctPath(hashAndName[0]), storage));
            }
        }
        return hashToName;
    }

    public List<CommitInfo> getBranchCommits(String branch) throws GitException {
        File logFile = gitTree.log().resolve(branch).toFile();
        List<CommitInfo> branchHistory = new ArrayList<>();
        if(!logFile.exists()) {
            return branchHistory;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            for (String line; (line = br.readLine()) != null; ) {
                if(!line.startsWith(BlobType.PARENT_BRANCH.asString())) {
                    branchHistory.add(gson.fromJson(line, CommitInfo.class));
                }
            }
        } catch (IOException e) {
            throw new GitException("error while reading log for " + branch);
        }
        return branchHistory;
    }
}
