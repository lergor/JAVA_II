package ru.ifmo.git.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import ru.ifmo.git.structs.CommitInfo;
import ru.ifmo.git.structs.FileReference;
import ru.ifmo.git.structs.HeadInfo;
import ru.ifmo.git.structs.Usages;
import ru.ifmo.git.tree.TreeEncoder;
import ru.ifmo.git.util.*;

public class GitLogger {

    private GitStructure git;
    private final Gson gson = new GsonBuilder().create();
    private static final String sep = System.getProperty("line.separator");

    private HeadInfo headInfo;
    private String currentTreeHash;
    private HashMap<String, Set<CommitInfo>> branchToHistory = new HashMap<>();

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
        commit.content = new StringBuilder()
                .append(commit.type.asString()).append(commitInfo.hash).append(sep)
                .append(treeInfo).append(commitInfo.branch).append(sep);
        return commit;
    }

    public String emptyLogResult() throws GitException {
        HeadInfo headInfo = getHeadInfo();
        return "fatal: your current branch '" +
                headInfo.branch() +
                "' does not have any commits yet" + sep;
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
        if(branchToHistory.containsKey(branch)) {
            return new ArrayList<>(branchToHistory.get(branch));
        }
        File logFile = git.log().resolve(branch).toFile();
        List<CommitInfo> history = new ArrayList<>();
        List<CommitInfo> previousBranchHistory = new ArrayList<>();
        if (!logFile.exists()) {
            branchToHistory.put(branch, new HashSet<>(history));
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
        branchToHistory.put(branch, new HashSet<>(history));
        return history;
    }

    public String currentTreeHash() throws GitException, IOException {
        if(currentTreeHash != null) {
            return currentTreeHash;
        }
        String currentCommit = getHeadInfo().currentHash();
        if (currentCommit.isEmpty()) {
            return "";
        }
        Path file = GitFileManager.pathInStorage(git.storage(), currentCommit);
        if (Files.exists(file)) {
            String treeLine = Files.readAllLines(file).get(1);
            currentTreeHash = TreeEncoder.withoutMarker(treeLine).split("\t")[0];
            return currentTreeHash;
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
        List<CommitInfo> commits = new ArrayList<>();
        List<CommitInfo> branchHistory = getHistory(branch);
        if (!logFile.exists()) {
            return branchHistory;
        }
        return branchHistory.stream().filter(i -> i.branch.equals(branch))
                .collect(Collectors.toList());
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
