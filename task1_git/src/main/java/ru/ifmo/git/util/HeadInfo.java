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

    public void setHeadHash(String hash) {
        headHash = hash;
    }

    public void setCurrentHash(String hash) {
        currentHash = hash;
    }
}
