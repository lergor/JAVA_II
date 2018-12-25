package ru.ifmo.torrent.client.storage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.ifmo.torrent.client.ClientConfig;
import ru.ifmo.torrent.client.storage.LocalFilesManager;
import ru.ifmo.torrent.client.storage.LocalFileReference;
import ru.ifmo.torrent.util.TorrentException;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


public class LocalFilesManagerTest {

    private static final long size = 17;
    private static final int id = 0;
    private static final String name = "kek";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testEmptyState() throws IOException, TorrentException {
        LocalFilesManager state = new LocalFilesManager(folder.getRoot().toPath());
        assertThat(state.getFiles()).isEmpty();
    }

    @Test
    public void testAddAndGet() throws IOException, TorrentException {
        LocalFilesManager state = new LocalFilesManager(folder.getRoot().toPath());
        state.addLocalFile(name, id, size);
        testGetFile(state);
    }

    @Test
    public void testAddAndContainsAfterReloading() throws IOException, TorrentException {
        Path metaFile = folder.newFile(ClientConfig.LOCAL_FILES_FILE).toPath();
        LocalFilesManager storedState = new LocalFilesManager(metaFile);

        storedState.addLocalFile(name, id, size);
        testGetFile(storedState);
        storedState.storeToFile();

        LocalFilesManager restoredState = new LocalFilesManager(metaFile);
        restoredState.restoreFromFile();
        testGetFile(restoredState);
    }

    private void testGetFile(LocalFilesManager state) {
        LocalFileReference fileState = state.getFileReference(id);
        assertThat(fileState.getFileId()).isEqualTo(id);
        assertThat(fileState.getName()).isEqualTo(name);
        assertThat(fileState.getNumberOfParts()).isEqualTo(1);
    }

}
