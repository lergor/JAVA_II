package ru.ifmo.torrent.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Config {

    public static final int TIMEOUT = 5 * 1000;

    private static final Path CWD = Paths.get(System.getProperty("user.dir")).normalize();

    protected static final Path TORRENT_DIR = CWD.resolve("torrent");
    protected static final Path TRACKER_STORAGE = TORRENT_DIR.resolve("tracker");
    protected static final Path CLIENT_STORAGE = TORRENT_DIR.resolve("client");

    public static Path checkAndCreateMetaDir(String subDirectory) {
        Path metaDir = TORRENT_DIR.resolve(subDirectory);
        if(Files.notExists(metaDir)) {
            metaDir.toFile().mkdirs();
        }
        return metaDir;
    }

    public static Path checkAndCreateFile(Path root, String file) throws IOException {
        Path filePath = root.resolve(file);
        if(Files.notExists(filePath)) {
            Files.createFile(filePath);
        }
        return filePath;
    }
}
