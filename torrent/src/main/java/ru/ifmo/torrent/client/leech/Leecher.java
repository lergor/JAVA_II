package ru.ifmo.torrent.client.leech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class Leecher implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Leecher.class);

    private short port;
    private final InetAddress address;


    public Leecher(short port, InetAddress address) throws IOException {
        logger.debug("leech " + address + " " + port);
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

    @Override
    public void close() throws TorrentException {
//        try {
//            socket.close();
//        } catch (IOException e) {
//            throw new TorrentException("peer: cannot close socket", e);
//        }
    }

    public List<Integer> getAvailableParts(int fileId) throws IOException {
        logger.debug("request getAvailableParts");
        StatResponse response = (StatResponse) sendRequest(new StatRequest(fileId));
        return response.getAvailableParts();
    }

    public byte[] getPartContent(int fileId, int part) throws IOException {
        logger.debug("request getPartContent");
        GetRequest request = new GetRequest(fileId, part);
        GetResponse response = (GetResponse) sendRequest(request);
        return response.getContent();
    }

}
