package ru.ifmo.git.structs;

import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
        this.rootDirectory = rootPath.toString();
    }

    public String toString() {
        String sep = System.lineSeparator();
        return "commit " + hash + "\t" + "(" + branch + ")" + sep +
                "Author:\t" + author + sep +
                "Date:\t" + time + sep +
                "\t\t" + message + sep + sep;
    }

}
