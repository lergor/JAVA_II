package ru.ifmo.torrent.tracker.state;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FileInfo {

    private int id;
    private String name;
    private long size;

    public FileInfo(int id, String name, long size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public static FileInfo readFrom(DataInputStream in) throws IOException {
        int ID = in.readInt();
        String name = in.readUTF();
        long size = in.readLong();
        return new FileInfo(ID, name, size);
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeInt(id);
        out.writeUTF(name);
        out.writeLong(size);
    }
}
