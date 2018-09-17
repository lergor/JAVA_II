package ru.ifmo.git.masters;

import com.google.gson.*;
import org.apache.commons.io.*;

import ru.ifmo.git.util.*;

import java.io.*;
import java.util.*;

public class FileMaster {

    static public List<CommitInfo> getHistory(File logFile) throws GitException {
        List<CommitInfo> history = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            for (String line; (line = br.readLine()) != null; ) {
                history.add(new GsonBuilder().create().fromJson(line, CommitInfo.class));
            }
        } catch (IOException e) {
            throw new GitException("error while reading log\n");
        }
        return history;
    }

    public static HeadInfo getHeadInfo() throws GitException {
        String headJson;
        try (FileInputStream inputStream = new FileInputStream(GitTree.head())) {
            headJson = IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new GitException("error while reading HEAD\n");
        }
        return new GsonBuilder().create().fromJson(headJson, HeadInfo.class);
    }

    static public void writeToFile(String fileName, String content, boolean append) throws GitException {
        File file = new File(fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
            writer.write(content + (append ? "\n" : ""));
        } catch (IOException e) {
            throw new GitException("error while writing to " + file.getName());
        }
    }

    static public void changeCurHash(String newHash, boolean withHead) throws GitException {
        HeadInfo headInfo = FileMaster.getHeadInfo();
        headInfo.setCurrentHash(newHash);
        if (withHead) {
            headInfo.setHeadHash(newHash);
        }
        String newHeadContent = (new Gson()).toJson(headInfo);
        FileMaster.writeToFile(GitTree.head(), newHeadContent, false);
    }

    static public File findCommitInStorage(String desiredCommit) throws GitException {
        File[] allCommits = new File(GitTree.storage()).listFiles();
        if (allCommits == null || allCommits.length == 0) {
            throw new GitException("no commits yet\n");
        }
        Optional<File> commitDir = Arrays.stream(allCommits)
                .filter(file -> file.getName().startsWith(desiredCommit)).findFirst();
        if (!commitDir.isPresent()) {
            throw new GitException("no such commit\n");
        }
        return commitDir.get();
    }

}
