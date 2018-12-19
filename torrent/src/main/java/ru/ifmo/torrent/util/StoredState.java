package ru.ifmo.torrent.util;

import java.io.IOException;
import java.nio.file.Path;

public interface StoredState {

    void restoreFromFile(Path file) throws IOException;

    void storeToFile(Path file) throws IOException;
}
