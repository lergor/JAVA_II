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

    public void setConflictingFiles(Set<String> conflictingFiles) {
        this.conflictingFiles = conflictingFiles;
    }

    public void setMergeBranch(String mergeBranch) {
        this.mergeBranch = mergeBranch;
    }

    public String mergeBranch() {
        return mergeBranch;
    }
}
