package ru.ifmo.git.util;

import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class CommitInfo {
    public String author;
    public String time;
    public String hash;
    public String message;
    public String branch;
    public String rootDirectory;

    public CommitInfo() {
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setAuthor() {
        this.author = System.getProperty("user.name");
    }

    public void setTime() {
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZ");
        this.time = df.format(Calendar.getInstance().getTime());
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setRootDirectory(Path rootPath) {
        this.rootDirectory = rootPath.toAbsolutePath().toString();
    }

    public String toString() {
        return "commit " + hash + "\n" +
                "Author:\t" + author + "\n" +
                "Date:\t" + time + "\n" +
                "\t\t" + message + "\n\n";
    }

}
