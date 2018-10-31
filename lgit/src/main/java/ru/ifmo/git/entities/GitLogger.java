package ru.ifmo.git.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ru.ifmo.git.structs.CommitInfo;
import ru.ifmo.git.structs.HeadInfo;
import ru.ifmo.git.structs.Usages;
import ru.ifmo.git.tree.TreeEncoder;
import ru.ifmo.git.util.*;

public class GitLogger {

    private GitStructure git;
    private final Gson gson = new GsonBuilder().create();
    private static final String ENCODING = "UTF-8";
    private static final String sep = System.getProperty("line.separator");

    private HeadInfo headInfo;

    GitLogger(GitStructure structure) {
        git = structure;
    }

    public CommitInfo fillCommitInfo(String message) throws GitException {
        CommitInfo info = new CommitInfo();
        info.setAuthor(getAuthor());
        info.setTime(getCurrentTime());
        info.setRootDirectory(git.repo().getFileName());
        info.setMessage(message == null ? getUserMessage() : message);
        info.setHash(createCommitHash(info));
        info.setBranch(getHeadInfo().branch());
        return info;
    }

    private static String createCommitHash(CommitInfo info) {
        String builder = info.time + info.rootDirectory +
                info.author + info.branch;
        return DigestUtils.sha1Hex(builder);
    }

    private static String getAuthor() {
        return System.getProperty("user.name");
    }

    private static String getCurrentTime() {
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZ");
        return df.format(Calendar.getInstance().getTime());
    }

    private static String getUserMessage() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("please enter message: ");
            return br.readLine();
        } catch (IOException e) {
            return "";
        }
    }

    public static FileReference formCommitReference(CommitInfo commitInfo, String treeInfo) {
        FileReference commit = new FileReference();
        commit.type = BlobType.COMMIT;
        commit.name = commitInfo.hash;
        commit.content = new SequenceInputStream(
                IOUtils.toInputStream(commit.type.asString() + commitInfo.hash + sep),
                IOUtils.toInputStream(treeInfo + commitInfo.branch + sep)
        );
        return commit;
    }

    public String emptyLogResult() throws GitException {
        HeadInfo headInfo = getHeadInfo();
        return "fatal: your current branch '" +
                headInfo.branch() +
                "' does not have any commits yet\n";
    }

    private static void writeToFile(Path file, String content, boolean append) throws GitException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile(), append))) {
            writer.write(content);
        } catch (IOException e) {
            throw new GitException("error while writing to " + file.getFileName());
        }
    }

    void changeHeadInfo(String hash, String branch) throws GitException {
        HeadInfo headInfo = getHeadInfo();
        headInfo.branchName = branch;
        changeHeadInfo(hash);
    }

    void changeHeadInfo(String hash) throws GitException {
        HeadInfo headInfo = getHeadInfo();
        if (headInfo.headHash().equals(headInfo.currentHash())) {
            headInfo.moveBoth(hash);
        } else {
            headInfo.moveCurrent(hash);
        }
        writeHeadInfo(headInfo);
    }

    public List<CommitInfo> getHistory() throws GitException {
        return getHistory(getHeadInfo().branch());
    }

    public List<CommitInfo> getHistory(String branch) throws GitException {
        File logFile = git.log().resolve(branch).toFile();
        List<CommitInfo> history = new ArrayList<>();
        List<CommitInfo> previousBranchHistory = new ArrayList<>();
        if (!logFile.exists()) {
            return history;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (line.startsWith(BlobType.BRANCH.asString())) {
                    previousBranchHistory = getHistory(line.substring(BlobType.size()));
                } else {
                    history.add(gson.fromJson(line, CommitInfo.class));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new GitException("error while reading log for " + branch);
        }
        Collections.reverse(history);
        history.addAll(previousBranchHistory);
        return history;
    }

    public String currentTreeHash() throws GitException, IOException {
        String currentCommit = getHeadInfo().currentHash();
        if (currentCommit.isEmpty()) {
            return "";
        }
        Path file = GitFileManager.pathInStorage(git.storage(), currentCommit);
        if (Files.exists(file)) {
            String treeLine = Files.readAllLines(file).get(1);
            return TreeEncoder.withoutMarker(treeLine).split("\t")[0];
        }
        throw new GitException("missing commit " + currentCommit);
    }

    String getHead(String branch) throws GitException {
        List<CommitInfo> history = getHistory(branch);
        if (history.size() == 0) {
            return "";
        }
        return history.get(0).hash;
    }

    void newLogFile(Path logFile) throws GitException, IOException {
        Files.createFile(logFile);
        String parentBranch = BlobType.BRANCH.asString() + getHeadInfo().branch() + sep;
        writeToFile(logFile, parentBranch, false);
    }

    public List<CommitInfo> getBranchCommits(String branch) throws GitException {
        File logFile = git.log().resolve(branch).toFile();
        List<CommitInfo> branchHistory = new ArrayList<>();
        if (!logFile.exists()) {
            return branchHistory;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (!line.startsWith(BlobType.BRANCH.asString())) {
                    branchHistory.add(gson.fromJson(line, CommitInfo.class));
                }
            }
        } catch (IOException e) {
            throw new GitException("error while reading log for branch '" + branch + "'");
        }
        return branchHistory;
    }

    private static <T> void writeInstance(T instance, File file, boolean append) throws GitException {
        try {
            if (file.exists() || file.createNewFile()) {
                writeToFile(file.toPath(), new GsonBuilder().create().toJson(instance) + sep, append);
            }
        } catch (IOException e) {
            throw new GitException("error while writing to " + file + ": " + e.getMessage());
        }
    }

    private static <T> T readInstance(Class<T> clazz, File file) throws GitException {
        String string;
        try {
            string = FileUtils.readFileToString(file);
        } catch (IOException e) {
            throw new GitException("error while reading " + file + ": " + e.getMessage());
        }
        return new GsonBuilder().create().fromJson(string, clazz);
    }

    public void writeUsages(Usages usages) throws GitException {
        GitLogger.writeInstance(usages, git.usageInfo().toFile(), false);
    }

    public Usages getUsages() throws GitException {
        return GitLogger.readInstance(Usages.class, git.usageInfo().toFile());
    }

    void writeHeadInfo(HeadInfo headInfo) throws GitException {
        this.headInfo = headInfo;
        GitLogger.writeInstance(headInfo, git.head().toFile(), false);
    }

    public HeadInfo getHeadInfo() throws GitException {
        if (headInfo == null) {
            headInfo = readInstance(HeadInfo.class, git.head().toFile());
        }
        return headInfo;
    }

    public void writeLog(CommitInfo commit) throws GitException {
        GitLogger.writeInstance(commit, git.log().resolve(commit.branch).toFile(), true);
    }
}
