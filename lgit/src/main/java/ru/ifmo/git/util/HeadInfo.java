package ru.ifmo.git.util;

public class HeadInfo {

    public String branchName;
    public String headHash;
    public String currentHash;

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

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
