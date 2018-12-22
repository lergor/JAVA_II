package ru.ifmo.torrent.client.leech;

import ru.ifmo.torrent.messages.Request;
import ru.ifmo.torrent.messages.Response;
import ru.ifmo.torrent.messages.seed_peer.requests.*;
import ru.ifmo.torrent.messages.seed_peer.response.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class Leecher {

    private short port;
    private final InetAddress address;


    public Leecher(short port, InetAddress address) {
        this.port = port;
        this.address = address;
    }

    public Response sendRequest(Request request) {
        try(Socket socket = new Socket(address, port)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out =  new DataOutputStream(socket.getOutputStream());
            request.write(out);
            out.flush();
            Response response = request.getEmptyResponse();
            response.read(in);
            return response;
        } catch (IOException e) {
            throw new IllegalStateException("cannot open leech", e);
        }
    }

    public List<Integer> getAvailableParts(int fileId) {
        StatResponse response = (StatResponse) sendRequest(new StatRequest(fileId));
        return response.getAvailableParts();
    }

    public byte[] getPartContent(int fileId, int part) throws IOException {
        GetRequest request = new GetRequest(fileId, part);
        GetResponse response = (GetResponse) sendRequest(request);
        return response.getContent();
    }

}
