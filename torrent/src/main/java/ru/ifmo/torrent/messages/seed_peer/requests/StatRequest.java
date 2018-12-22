package ru.ifmo.torrent.messages.seed_peer.requests;

import ru.ifmo.torrent.messages.seed_peer.Marker;
import ru.ifmo.torrent.messages.seed_peer.response.StatResponse;
import ru.ifmo.torrent.messages.Request;
import ru.ifmo.torrent.messages.Response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StatRequest extends Request {
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
    public Response getEmptyResponse() {
        return new StatResponse();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(marker());
        out.writeInt(fileID);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        fileID = in.readInt();
    }

    public int getFileID() {
        return fileID;
    }

    public static StatRequest readFromDataInputStream(DataInputStream in) throws IOException {
        StatRequest request = new StatRequest();
        request.read(in);
        return request;
    }
}
