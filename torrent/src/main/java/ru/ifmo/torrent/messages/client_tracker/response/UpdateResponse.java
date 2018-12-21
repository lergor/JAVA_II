package ru.ifmo.torrent.messages.client_tracker.response;

import ru.ifmo.torrent.messages.client_tracker.TrackerResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class UpdateResponse extends TrackerResponse {
    private boolean success;

    public UpdateResponse() {
    }

    public UpdateResponse(boolean success) {
        this.success = success;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeBoolean(success);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        success = in.readBoolean();
    }

    @Override
    public void printTo(PrintStream printer) {
    }

    public boolean getResult() {
        return success;
    }

}
