package ru.ifmo.torrent.client;

import org.junit.BeforeClass;
import ru.ifmo.torrent.tracker.Tracker;
import ru.ifmo.torrent.tracker.TrackerConfig;

public class TorrentTest {

    private static final int THREADS_NUMBER = 4;

    @BeforeClass
    public static void startTracker() throws Exception {
        final int SERVER_RUNNING_TIME = 50;
        final Tracker tracker = new Tracker(TrackerConfig.TRACKER_PORT);

        final Thread serverThread = new Thread(tracker);
        serverThread.start();

        try {
            Thread.sleep(SERVER_RUNNING_TIME);
        } catch (InterruptedException ignored) {
        }
    }


}
