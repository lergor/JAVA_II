package ru.ifmo.git.structs;

import ru.ifmo.git.tree.Tree;
import ru.ifmo.git.util.GitException;

import java.io.IOException;
import java.util.*;

public class StatusInfo {

    private Status indexToHead; // tracked
    private Status repoToHead; // untracked

    private static final String sep = System.lineSeparator();

    public StatusInfo(Tree repo, Tree headCommit, Tree index) throws IOException, GitException {
        repoToHead = new Status(repo, headCommit);
        indexToHead = new Status(index, headCommit);

        repoToHead.getNewFiles().removeAll(indexToHead.getNewFiles());
        repoToHead.getDeletedFiles().removeAll(indexToHead.getDeletedFiles());
        repoToHead.getModifiedFiles().removeAll(indexToHead.getModifiedFiles());
    }

    Set<String> getNew(boolean tracked) {
        return tracked ? indexToHead.getNewFiles() : repoToHead.getNewFiles();
    }

    Set<String> getModified(boolean tracked) {
        return tracked ? indexToHead.getModifiedFiles() : repoToHead.getModifiedFiles();
    }

    public Set<String> getDeleted(boolean tracked) {
        return tracked ? indexToHead.getDeletedFiles() : repoToHead.getDeletedFiles();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(indexToHead.isEmpty() ? "" : "Tracked files:" + sep);
        builder.append(indexToHead.newFilesToString())
                .append(indexToHead.modifiedFilesToString())
                .append(indexToHead.deletedFilesToString());
        builder.append(repoToHead.isEmpty() ? "" : "Untracked files:" + sep);
        builder.append(repoToHead.newFilesToString())
                .append(repoToHead.modifiedFilesToString())
                .append(repoToHead.deletedFilesToString());
        return builder.toString();
    }

    public boolean isEmpty() {
        return repoToHead.isEmpty() && indexToHead.isEmpty();
    }
}
