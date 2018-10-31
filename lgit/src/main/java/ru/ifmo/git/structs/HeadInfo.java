package ru.ifmo.git.structs;

public class HeadInfo {

    public String branchName = "";
    public String headHash = "";
    public String currentHash = "";

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
}
