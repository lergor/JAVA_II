package ru.ifmo.torrent.tracker;

import ru.ifmo.torrent.tracker.state.TrackerState;
import ru.ifmo.torrent.util.TorrentException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Tracker implements AutoCloseable, Runnable {

    private final ExecutorService pool = Executors.newFixedThreadPool(TrackerConfig.THREADS_COUNT);
    private final short port;
    private final TrackerState state = new TrackerState();

    public Tracker(short port) throws TorrentException {
        Path metaDir = TrackerConfig.getStorage();
        this.port = port;
        try {
            state.restoreFromFile(metaDir.resolve(TrackerConfig.getTrackerStateFile()));
        } catch (IOException e) {
            throw new TorrentException("cannot read meta info about available files", e);
        }
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("tracker started at port " + port);
//            pool.submit(new SeedListUpdater());

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("cleint " + client.getInetAddress() + " " + client.getPort());
                pool.submit(new ClientHandler(client, state));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws TorrentException {
        try {
            System.out.println("CLOSE");
            state.storeToFile(TrackerConfig.getStorage().resolve(TrackerConfig.getTrackerStateFile()));
        } catch (IOException e) {
            throw new TorrentException("cannot write meta info about available files", e);
        }

    }

}
