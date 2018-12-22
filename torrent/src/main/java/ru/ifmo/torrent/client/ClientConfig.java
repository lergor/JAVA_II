package ru.ifmo.torrent.client;

import ru.ifmo.torrent.util.Config;

import java.nio.file.Files;
import java.nio.file.Path;

public class ClientConfig extends Config {

    public static final int FILE_PART_SIZE = 1024 * 1024 * 10;

    private ClientConfig() {
    }

    public static Path getMetaDir() {
        Path storage = CLIENT_STORAGE;
        if(Files.notExists(storage)) {
            storage.toFile().mkdirs();
        }
        return storage;
    }
}
