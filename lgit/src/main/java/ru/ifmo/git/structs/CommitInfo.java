package ru.ifmo.git.structs;

import java.nio.file.Path;

public class CommitInfo {
    public String author;
    public String time;
    public String hash;
    public String message;
    public String branch;
    public String rootDirectory;
    public String treeHash;
    public String prevCommitHash;

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setRootDirectory(Path rootPath) {
        this.rootDirectory = rootPath.toString();
    }

    public String toString() {
        String sep = System.lineSeparator();
        return "commit " + hash + "\t" + "(" + branch + ")" + sep +
                "Author:\t" + author + sep +
                "Date:\t" + time + sep +
                "\t\t" + message + sep + sep;
    }

    public String author() {
        return author;
    }

    public String branch() {
        return branch;
    }

    public String hash() {
        return hash;
    }

    public String rootDirectory() {
        return rootDirectory;
    }

    public String time() {
        return time;
    }

    public void setTreeHash(String treeHash) {
        this.treeHash = treeHash;
    }

    public String treeHash() {
        return treeHash;
    }

    public void setPrevCommitHash(String prevCommitHash) {
        this.prevCommitHash = prevCommitHash;
    }

    public String previousCommitHash() {
        return prevCommitHash;
    }
}
