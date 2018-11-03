package ru.ifmo.git.structs;

import java.util.HashSet;
import java.util.Set;

public class HeadInfo {

    public String branchName = "";
    public String headHash = "";
    public String currentHash = "";
    public Boolean mergeConflict = false;
    public Set<String> conflictingFiles = new HashSet<>();
    public String mergeBranch = "";
    public boolean merging = false;

    public HeadInfo() {
        branchName = "master";
    }

    public void moveHead(String hash) {
        headHash = hash;
    }

    public void moveCurrent(String hash) {
        currentHash = hash;
    }

    public void moveBoth(String hash) {
        moveHead(hash);
        moveCurrent(hash);
    }

    public String currentHash() {
        return currentHash;
    }

    public String headHash() {
        return headHash;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String branch() {
        return branchName;
    }

    public Boolean mergeConflictFlag() {
        return mergeConflict;
    }

    public Set<String> getConflictingFiles() {
        return conflictingFiles;
    }

    public String getConflictingFilesAsString() {
        StringBuilder builder = new StringBuilder();
        String sep = System.lineSeparator();
        conflictingFiles.forEach( f -> builder.append("\t").append(f).append(sep));
        return builder.toString();
    }

    public void setConflictingFiles(Set<String> conflictingFiles) {
        this.conflictingFiles = conflictingFiles;
    }

    public void setMergeBranch(String mergeBranch) {
        this.mergeBranch = mergeBranch;
    }

    public String mergeBranch() {
        return mergeBranch;
    }

    public boolean merging() {
         return merging;
    }

    public void setMerging(boolean merging) {
        this.merging = merging;
    }

    public void setMergeConflict(Boolean mergeConflict) {
        this.mergeConflict = mergeConflict;
    }
}
