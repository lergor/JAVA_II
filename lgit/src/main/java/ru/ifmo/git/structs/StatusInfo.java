package ru.ifmo.git.structs;

import ru.ifmo.git.tree.Tree;
import ru.ifmo.git.util.GitException;

import java.io.IOException;
import java.util.*;

public class StatusInfo {

    private Status indexToHead; // tracked
    private Status repoToIndex; // untracked

    private static final String sep = System.lineSeparator();

    public StatusInfo(Tree repo, Tree headCommit, Tree index) throws IOException, GitException {
        repoToIndex = new Status(repo, index);
        indexToHead = new Status(index, headCommit);
    }

    public Set<String> getDeleted(boolean tracked) {
        return tracked ? indexToHead.getDeletedFiles() : repoToIndex.getDeletedFiles();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(indexToHead.isEmpty() ? "" : "Tracked files:" + sep);
        builder.append(indexToHead.newFilesToString())
                .append(indexToHead.modifiedFilesToString())
                .append(indexToHead.deletedFilesToString());
        builder.append(repoToIndex.isEmpty() ? "" : "Untracked files:" + sep);
        builder.append(repoToIndex.newFilesToString())
                .append(repoToIndex.modifiedFilesToString())
                .append(repoToIndex.deletedFilesToString());
        return builder.toString();
    }

    public boolean isEmpty() {
        return repoToIndex.isEmpty() && indexToHead.isEmpty();
    }
}
