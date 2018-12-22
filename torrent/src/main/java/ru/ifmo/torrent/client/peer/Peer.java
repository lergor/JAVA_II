package ru.ifmo.torrent.client.peer;

import ru.ifmo.torrent.tracker.state.SeedInfo;
import ru.ifmo.torrent.util.TorrentException;

import java.io.IOException;
import java.net.Socket;

public class Peer implements AutoCloseable {

    private Socket socket;
    private final SeedInfo seed;

    Peer(SeedInfo seed) {
        this.seed = seed;
    }

    public void run() throws TorrentException {
        try {
            socket = new Socket(seed.inetAddress(), seed.port());
        } catch (IOException e) {
            throw new TorrentException("cannot open seed socket", e);
        }
    }

    //        public BitSet getAvailablePartsInfo(int fileId) {
//            StatRequest statRequest = new StatRequest(fileId);
//            StatResponse response = statRequest.handleQuery(socket);
//            return response.getParts();
//        }
//
//        public byte[] getPart(int fileId, int partNumber) {
//            GetRequest getRequest = new GetRequest(fileId, partNumber);
//            GetResponse response = getRequest.handleQuery(socket);
//            return response.getPartContent();
//        }

    @Override
    public void close() throws TorrentException {
        try {
            socket.close();
        } catch (IOException e) {
            throw new TorrentException("peer: cannot close socket", e);
        }
    }

}
