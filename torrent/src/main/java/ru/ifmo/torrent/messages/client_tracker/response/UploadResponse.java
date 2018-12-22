package ru.ifmo.torrent.messages.client_tracker.response;

import ru.ifmo.torrent.messages.Response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UploadResponse extends Response {
    private int fileID;

    public UploadResponse() {
    }

    public UploadResponse(int fileID) {
        this.fileID = fileID;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(fileID);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        fileID = in.readInt();
    }

    public int getFileID() {
        return fileID;
    }
}
