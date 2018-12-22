package ru.ifmo.torrent.client.seed;

import ru.ifmo.torrent.client.state.LocalFilesManager;
import ru.ifmo.torrent.util.TorrentException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Seed implements Runnable, AutoCloseable {

    private final short port;
    private final LocalFilesManager filesManager;
    private ServerSocket socket;

    public Seed(short port, LocalFilesManager filesManager) {
        this.port = port;
        this.filesManager = filesManager;
    }

    public void run() {
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        try (ServerSocket socket = new ServerSocket(port)) {
            this.socket = socket;
            while (true) {
                Socket peerSocket = socket.accept();
                threadPool.submit(new LeechHandler(peerSocket, filesManager));
            }
        } catch (IOException e) {
            System.err.println("cannot open seed socket\n" + e.getMessage());

        }
    }

    public short getPort() {
        return (short) socket.getLocalPort();
    }

    @Override
    public void close() throws TorrentException {
        try {
            socket.close();
        } catch (IOException e) {
            throw new TorrentException("seed: cannot close socket", e);
        }
    }
}
