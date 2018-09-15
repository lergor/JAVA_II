package ru.ifmo.git.util;

public class BranchInfo {

    public String branchName;
    public String headHash;
    public String historyFilePath;
    public String logFilePath;
    public String storagePath;

    public BranchInfo() {
    }

    public BranchInfo(String branch, String head) {
        branchName = branch;
        headHash = head;
        historyFilePath = "info/hist_" + branchName;
        logFilePath = "logs/" + branchName;
        storagePath = "storage/" + headHash;
    }
}
