package ru.ifmo.torrent.client.seed;

import ru.ifmo.torrent.client.state.LocalFilesManager;
import ru.ifmo.torrent.util.TorrentException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Seed {

    private ServerSocket socket;

    public void start(short port, LocalFilesManager localFilesManager) {
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        try {
            socket = new ServerSocket(port);
            while (true) {
                Socket peerSocket = socket.accept();
                threadPool.submit(new PeerHandler(peerSocket, localFilesManager));
            }
        } catch (IOException e) {
            System.err.println("cannot open peer socket\n" + e.getMessage());

        }
    }

    public void stop() throws TorrentException {
        try {
            socket.close();
        } catch (IOException e) {
            throw new TorrentException("seed: cannot close socket", e);
        }
    }

    public short getPort() {
        return (short) socket.getLocalPort();
    }
}
