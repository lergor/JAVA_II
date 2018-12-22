package ru.ifmo.torrent.client.seed;

import ru.ifmo.torrent.client.storage.LocalFilesManager;
import ru.ifmo.torrent.util.TorrentException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Seeder implements Runnable, AutoCloseable {

    private final short port;
    private final LocalFilesManager filesManager;
    private final ServerSocket socket;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(4);


    public Seeder(short port, LocalFilesManager filesManager) throws IOException {
        this.port = port;
        this.filesManager = filesManager;
        socket = new ServerSocket(port);
    }

    public void run() {
        try (ServerSocket socket = this.socket) {
            while (true) {
                Socket leecherSocket = socket.accept();
                threadPool.submit(new LeechHandler(leecherSocket, filesManager));
            }
        } catch (IOException e) {
            if(!socket.isClosed()) {
                throw new IllegalStateException("cannot close seed socket\n" + e.getMessage());
            }
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
        } finally {
            threadPool.shutdown();
        }
    }
}
