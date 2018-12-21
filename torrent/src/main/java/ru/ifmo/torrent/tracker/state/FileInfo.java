package ru.ifmo.torrent.tracker.state;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FileInfo {

    private int ID;
    private String name;
    private long size;

    public FileInfo() {
    }

    public FileInfo(int ID, String name, long size) {
        this.ID = ID;
        this.name = name;
        this.size = size;
    }

    public int fileID() {
        return ID;
    }

    public long size() {
        return size;
    }

    public String name() {
        return name;
    }

    public static FileInfo readFileInfo(DataInputStream in) throws IOException {
        int ID = in.readInt();
        String name = in.readUTF();
        long size = in.readLong();
        return new FileInfo(ID, name, size);
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeInt(ID);
        out.writeUTF(name);
        out.writeLong(size);
    }
}
