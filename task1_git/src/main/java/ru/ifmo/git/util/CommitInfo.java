package ru.ifmo.git.util;

public class CommitInfo {
    public String author;
    public String time;
    public String hash;
    public String message;
    public String branch;

    public CommitInfo() {}

    public String toString() {
        return  "commit " + hash + "\n" +
                "Author:\t" + author + "\n" +
                "Date:\t" + time + "\n" +
                "\t\t" + message + "\n\n";
    }
}
