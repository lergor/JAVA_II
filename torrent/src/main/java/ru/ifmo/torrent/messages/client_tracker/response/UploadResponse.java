package ru.ifmo.torrent.messages.client_tracker.response;

import ru.ifmo.torrent.messages.client_tracker.TrackerResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class UploadResponse extends TrackerResponse {
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

    @Override
    public void printTo(PrintStream printer) {
        printer.printf("file added with id: %d%n", fileID);
    }

    public int getFileID() {
        return fileID;
    }
}
