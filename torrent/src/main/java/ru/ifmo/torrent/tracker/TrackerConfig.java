package ru.ifmo.torrent.tracker;

import ru.ifmo.torrent.util.Config;

import java.nio.file.Files;
import java.nio.file.Path;

public class TrackerConfig extends Config {

    public static final short TRACKER_PORT = 8081;
    public static final int THREADS_COUNT = 8;

    public static final String ID_TO_FILE = "id_to_file";
    public static final String ID_TO_CLIENT = "id_to_client";
    public static final String CLIENT_LAST_UPD = "client_last_upd";
    public static final String TRACKER_STATE_FILE = "tracker_state_file";

    private TrackerConfig() {
    }

    public static Path getStorage() {
        Path storage = TRACKER_STORAGE;
        if(Files.notExists(storage)) {
            storage.toFile().mkdirs();
        }
        return storage;
    }

    public static Path getTrackerStateFile() {
        Path storage = getStorage();
        return storage.resolve(TRACKER_STATE_FILE);
    }
}
