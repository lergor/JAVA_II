package ru.ifmo.torrent.util;

import java.io.IOException;
import java.nio.file.Path;

public interface StoredState {

    void restoreFromFile() throws IOException;

    void storeToFile() throws IOException;
}
