package ru.ifmo.torrent.tracker;

import ru.ifmo.torrent.tracker.state.TrackerState;
import ru.ifmo.torrent.util.TorrentException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Tracker implements AutoCloseable, Runnable {

    private final ExecutorService pool = Executors.newFixedThreadPool(TrackerConfig.THREADS_COUNT);
    private final short port;
    private final TrackerState state;
    private final ServerSocket serverSocket;

    public Tracker(short port) throws TorrentException {
        this.port = port;
        try {
            state = new TrackerState(TrackerConfig.getTrackerStateFile());
        } catch (IOException e) {
            throw new TorrentException("cannot read meta info about available files", e);
        }

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new TorrentException("cannot open server socket", e);
        }
    }

    @Override
    public void run() {
        pool.submit(() -> {
            System.out.println("tracker started at port " + port);
//            pool.submit(new SeedListUpdater());

            try {
                while (!Thread.interrupted()) {
                    Socket client = serverSocket.accept();
                    pool.submit(new ClientHandler(client, state));
                }
            } catch (IOException ignored) {
            }
        });
    }

    @Override
    public void close() throws TorrentException {
        try {
            serverSocket.close();
            state.storeToFile();
            pool.shutdown();
        } catch (IOException e) {
            throw new TorrentException("cannot write meta info about available files", e);
        }

    }

}
