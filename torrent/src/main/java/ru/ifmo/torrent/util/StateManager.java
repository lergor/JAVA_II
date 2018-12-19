package ru.ifmo.torrent.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface StateManager {

    void restoreState() throws IOException;

    void saveState() throws IOException;
}
