package ru.ifmo.torrent.messages.seed_peer.requests;

import ru.ifmo.torrent.messages.seed_peer.Marker;
import ru.ifmo.torrent.messages.seed_peer.response.GetResponse;
import ru.ifmo.torrent.network.Request;
import ru.ifmo.torrent.network.Response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetRequest extends Request {

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
    public Response getEmptyResponse() {
        return new GetResponse();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(marker());
        out.writeInt(fileID);
        out.writeInt(part);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        fileID = in.readInt();
        part = in.readInt();
    }

    public int getFileID() {
        return fileID;
    }

    public int getPart() {
        return part;
    }
}
