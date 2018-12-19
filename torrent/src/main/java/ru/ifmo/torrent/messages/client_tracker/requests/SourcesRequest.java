package ru.ifmo.torrent.messages.client_tracker.requests;

import ru.ifmo.torrent.messages.TorrentMessage;
import ru.ifmo.torrent.messages.TorrentResponse;
import ru.ifmo.torrent.messages.client_tracker.Marker;
import ru.ifmo.torrent.messages.client_tracker.ClientRequest;
import ru.ifmo.torrent.messages.client_tracker.response.SourcesResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SourcesRequest extends ClientRequest implements TorrentMessage {
    private int fileID;

    public SourcesRequest() {}

    public SourcesRequest(int fileID) {
        this.fileID = fileID;
    }

    @Override
    public byte marker() {
        return Marker.SOURCES;
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

    @Override
    public TorrentResponse execute() {
        System.out.println("sources exec " + fileID);
        // FIXME if list is empty - get error
        return new SourcesResponse(fileID, trackerState.getSources(fileID));
    }
}
