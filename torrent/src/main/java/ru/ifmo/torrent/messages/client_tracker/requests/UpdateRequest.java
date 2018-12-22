package ru.ifmo.torrent.messages.client_tracker.requests;

import ru.ifmo.torrent.messages.Request;
import ru.ifmo.torrent.messages.Response;
import ru.ifmo.torrent.messages.client_tracker.Marker;
import ru.ifmo.torrent.messages.client_tracker.response.UpdateResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateRequest extends Request {
    private short clientPort;
    private List<Integer> fileIds;

    public UpdateRequest() {
        fileIds = new ArrayList<>();
    }

    public UpdateRequest(short clientPort, List<Integer> fileIds) {
        this.clientPort = clientPort;
        this.fileIds = fileIds;
    }

    @Override
    public byte marker() {
        return Marker.UPDATE;
    }

    @Override
    public Response getEmptyResponse() {
        return new UpdateResponse();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(marker());
        out.writeShort(clientPort);
        out.writeInt(fileIds.size());
        for (Integer ID : fileIds) {
            out.writeInt(ID);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        clientPort = in.readShort();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            int fileID = in.readInt();
            fileIds.add(fileID);
        }
    }

    public short getClientPort() {
        return clientPort;
    }

    public List<Integer> getFileIds() {
        return fileIds;
    }

    public static UpdateRequest readFromDataInputStream(DataInputStream in) throws IOException {
        UpdateRequest request = new UpdateRequest();
        request.read(in);
        return request;
    }
}