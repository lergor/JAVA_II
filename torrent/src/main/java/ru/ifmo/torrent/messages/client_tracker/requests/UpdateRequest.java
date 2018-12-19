package ru.ifmo.torrent.messages.client_tracker.requests;

import ru.ifmo.torrent.messages.TorrentMessage;
import ru.ifmo.torrent.messages.TorrentResponse;
import ru.ifmo.torrent.messages.client_tracker.Marker;
import ru.ifmo.torrent.messages.client_tracker.ClientRequest;
import ru.ifmo.torrent.messages.client_tracker.response.UpdateResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateRequest extends ClientRequest implements TorrentMessage {
    private short clientPort;
    private List<Integer> fileIDs;

    public UpdateRequest() {
        fileIDs = new ArrayList<>();
    }

    public UpdateRequest(short clientPort, List<Integer> filesIDs) {
        this.clientPort = clientPort;
        this.fileIDs = filesIDs;
    }

    @Override
    public byte marker() {
        return Marker.UPDATE;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(marker());
        out.writeShort(clientPort);
        out.writeInt(fileIDs.size());
        for (Integer ID: fileIDs) {
            out.writeInt(ID);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        clientPort = in.readShort();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            int fileID = in.readInt();
            fileIDs.add(fileID);
        }
    }

    @Override
    public TorrentResponse execute() {
        // FIXME - something goes wrong
        System.out.println("update for " + client.inetAddress() + " " + clientPort);
        for (int ID : fileIDs) {
            trackerState.addNewSeedIfAbsent(ID, client);
        }
        return new UpdateResponse(true);
    }
}