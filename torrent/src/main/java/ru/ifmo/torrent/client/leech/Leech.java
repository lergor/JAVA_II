package ru.ifmo.torrent.client.leech;

import ru.ifmo.torrent.client.state.LocalFilesManager;
import ru.ifmo.torrent.messages.Request;
import ru.ifmo.torrent.messages.Response;
import ru.ifmo.torrent.messages.seed_peer.requests.GetRequest;
import ru.ifmo.torrent.messages.seed_peer.requests.StatRequest;
import ru.ifmo.torrent.messages.seed_peer.response.GetResponse;
import ru.ifmo.torrent.messages.seed_peer.response.StatResponse;
import ru.ifmo.torrent.util.TorrentException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class Leech implements AutoCloseable {

    private Socket socket;
    private short port;
    private final InetAddress address;
    private final DataInputStream in;
    private final DataOutputStream out;

    public Leech(short port, InetAddress address) throws IOException {
        this.port = port;
        this.address = address;
        socket = new Socket(address, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public Response sendRequest(Request request) throws IOException {
        Response response = null;
        request.write(out);
        out.flush();
        response = request.getEmptyResponse();
        response.read(in);
        return response;
    }

    @Override
    public void close() throws TorrentException {
        try {
            socket.close();
        } catch (IOException e) {
            throw new TorrentException("peer: cannot close socket", e);
        }
    }

    public List<Integer> getAvailableParts(int fileId) throws IOException {
        StatResponse response = (StatResponse) sendRequest(new StatRequest(fileId));
        return response.getAvailableParts();
    }

    public InputStream getPartContent(int fileId, int part) throws IOException {
        GetRequest request = new GetRequest(fileId, part);
        request.write(out);
        out.flush();
        return in;
    }

}
