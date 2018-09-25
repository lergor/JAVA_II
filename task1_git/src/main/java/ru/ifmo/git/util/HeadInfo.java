package ru.ifmo.git.util;

public class HeadInfo {

    public String branchName;
    public String headHash;
    public String currentHash;

    public HeadInfo() {
    }

    public HeadInfo(String branch) {
        branchName = branch;
        headHash = "";
        currentHash = "";
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

}
