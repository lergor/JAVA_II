package ru.ifmo.git.util;

public class HeadInfo {

    public String branchName;
    public String headHash;
    public String historyFilePath;
    public String logFilePath;
    public String storagePath;

    public HeadInfo() {
    }

    public HeadInfo(String branch) {
        branchName = branch;
        headHash = "";
        historyFilePath = "info/hist_" + branchName;
        logFilePath = "logs/" + branchName;
        storagePath = "storage";
    }

    public void setHeadHash(String hash) {
        headHash = hash;
        storagePath = "storage/" + hash;

    }
}
