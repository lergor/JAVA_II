package ru.ifmo.git.util;

public class HeadInfo {

    public String branchName;
    public String headHash;
    public String currentHash;
    public String logFilePath;

    public HeadInfo() {
    }

    public HeadInfo(String branch) {
        branchName = branch;
        headHash = "";
        currentHash = "";
        logFilePath = "logs/" + branchName;
    }

    public void setHeadHash(String hash) {
        headHash = hash;
    }

    public void setCurrentHash(String hash) {
        if(currentHash.equals(headHash)) {
            headHash = hash;
        }
        currentHash = hash;
    }
}
