package ru.ifmo.torrent.client;

import ru.ifmo.torrent.util.Config;

import java.nio.file.Files;
import java.nio.file.Path;

public class ClientConfig extends Config {

    public static final int FILE_PART_SIZE = 1024 * 1024 * 10;
    public static final int UPDATE_RATE = 10 * 1000;

    public static final String PARTS_STORAGE = "parts";
    public static final String LOCAL_FILES_FILE = "local_files";

    private ClientConfig() {
    }

    public static Path getMetaDir() {
        Path storage = CLIENT_STORAGE;
        if(Files.notExists(storage)) {
            storage.toFile().mkdirs();
        }
        return storage;
    }

    public static Path getLocalFilesStorage() {
        return getMetaDir().resolve(PARTS_STORAGE);
    }

    public static Path getLocalFilesFile() {
        return getMetaDir().resolve(LOCAL_FILES_FILE);
    }
}
