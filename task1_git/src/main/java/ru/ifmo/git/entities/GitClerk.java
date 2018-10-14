package ru.ifmo.git.entities;

import com.google.gson.*;
import org.apache.commons.io.*;

import ru.ifmo.git.commands.Git;
import ru.ifmo.git.util.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.stream.Collectors;

public class GitClerk {

    public static final String ENCODING = "UTF-8";
    public static final String sep = System.getProperty("line.separator");
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

    void writeHeadInfo(HeadInfo newHeadInfo) throws GitException {
        String newInfo = gson.toJson(newHeadInfo);
        try {
            FileUtils.writeStringToFile(gitTree.head().toFile(), newInfo, ENCODING);
        } catch (IOException e) {
            throw new GitException(e.getMessage());
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
            if(logFile.exists() || logFile.createNewFile()) {
                writeToFile(logFile.toPath(), gson.toJson(commit) + sep, true);
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

    public List<CommitInfo> getLogHistory() throws GitException {
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
        Collections.reverse(history);
        return history;
    }

    public Optional<CommitInfo> findCommitInfo(String desiredCommit) throws GitException {
        for (CommitInfo commit: getLogHistory()) {
            if(commit.hash.startsWith(desiredCommit)) {
                return Optional.of(commit);
            }
        }
        return Optional.empty();
    }

    public static String getAuthor() {
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

    String emptyLogResult() throws GitException {
        HeadInfo headInfo = getHeadInfo();
        return "fatal: your current branch '" +
                headInfo.branchName +
                "' does not have any commits yet\n";
    }

    public Map<String, String> compareRepoAndIndex() throws GitException {
        try {
            Map<String, String> fileToStatus = new HashMap<>();
            Map<String, String> inIndex = collectNameAndHash(gitTree.index());
            for (Map.Entry<String, String> e : inIndex.entrySet()) {
                Path fileInCWD = gitTree.repo().resolve(e.getKey());
                Path fileInIndex = gitTree.index().resolve(e.getKey());
                if (Files.isRegularFile(fileInIndex)) {
                    if (!Files.exists(fileInCWD)) {
                        fileToStatus.put(e.getKey(), "deleted");
                    } else {
                        String hashInIndex = e.getValue();
                        String hashInCWD = GitEncoder.getHash(fileInCWD, gitTree.repo());
                        if (!hashInCWD.equals(hashInIndex)) {
                            fileToStatus.put(e.getKey(), "modified");
                        }
                    }
                }
            }
            Files.list(gitTree.repo()).forEach(f -> {
                        String fileName = gitTree.repo().relativize(f).toString();
                        if(!fileName.equals(".l_git") && !Files.exists(gitTree.index().resolve(fileName))) {
                            fileToStatus.put(fileName, "new");
                        }
                    }
            );
            return fileToStatus;
        } catch (IOException e) {
            throw new GitException(e.getMessage());
        }
    }

    private Map<String, String> collectNameAndHash(Path directory) throws IOException {
        Map<String, String> nameToHash = new HashMap<>();
        List<Path> files = Files.list(directory).collect(Collectors.toList());
        for (Path file : files) {
            String name = file.toFile().getName();
            if (!name.equals(".l_git")) {
                nameToHash.put(name, GitEncoder.getHash(file, directory));
                if (Files.isDirectory(file)) {
                    Set<Map.Entry<String, String>> fromSubDir = collectNameAndHash(file).entrySet();
                    for (Map.Entry<String, String> e : fromSubDir) {
                        nameToHash.put(Paths.get(name, e.getKey()).toString(), e.getValue());
                    }
                }
            }
        }
        return nameToHash;
    }
}
