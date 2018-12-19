package ru.ifmo.torrent.messages.seed_peer.requests;

import ru.ifmo.torrent.messages.TorrentResponse;
import ru.ifmo.torrent.messages.seed_peer.ClientRequest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StatRequest extends ClientRequest {
    private int fileID;

    public StatRequest() {
    }

    public StatRequest(int fileID) {
        this.fileID = fileID;
    }

    @Override
    public byte marker() {
        return Marker.STAT;
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
    public TorrentResponse execute() {
        return null;
    }

}
