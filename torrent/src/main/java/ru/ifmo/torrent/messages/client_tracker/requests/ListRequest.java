package ru.ifmo.torrent.messages.client_tracker.requests;

import ru.ifmo.torrent.messages.TorrentResponse;
import ru.ifmo.torrent.messages.client_tracker.Marker;
import ru.ifmo.torrent.messages.client_tracker.ClientRequest;
import ru.ifmo.torrent.messages.client_tracker.response.ListResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ListRequest extends ClientRequest {

    @Override
    public byte marker() {
        return Marker.LIST;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(marker());
    }

    @Override
    public void read(DataInputStream in) {
    }

    @Override
    public TorrentResponse execute() {
        return new ListResponse(trackerState.getAvailableFiles());
    }
}
