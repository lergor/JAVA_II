package ru.ifmo.torrent.messages.client_tracker.requests;

import ru.ifmo.torrent.messages.client_tracker.Marker;
import ru.ifmo.torrent.messages.client_tracker.response.ListResponse;
import ru.ifmo.torrent.messages.Request;
import ru.ifmo.torrent.messages.Response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ListRequest extends Request {

    public ListRequest() {}

    @Override
    public byte marker() {
        return Marker.LIST;
    }

    @Override
    public Response getEmptyResponse() {
        return new ListResponse();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(marker());
    }

    @Override
    public void read(DataInputStream in) {}

}
