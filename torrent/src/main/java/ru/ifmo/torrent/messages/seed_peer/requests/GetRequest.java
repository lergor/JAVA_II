package ru.ifmo.torrent.messages.seed_peer.requests;

import ru.ifmo.torrent.messages.TorrentResponse;
import ru.ifmo.torrent.messages.seed_peer.ClientRequest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetRequest extends ClientRequest {

    private int fileID;
    private int part;

    public GetRequest() {
    }

    public GetRequest(int fileID, int part) {
        this.fileID = fileID;
        this.part = part;
    }

    @Override
    public byte marker() {
        return Marker.GET;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(marker());
        out.writeInt(fileID);
        out.writeInt(part);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        fileID = in.readInt();
        part = in.readInt();
    }

    @Override
    public TorrentResponse execute() {
        return null;
    }

}
