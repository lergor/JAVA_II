package ru.ifmo.torrent.client;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.ifmo.torrent.client.state.LocalFilesManager;
import ru.ifmo.torrent.client.state.LocalFileReference;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class LocalFilesManagerTest {

    private static final long size = 17;
    private static final int id = 0;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testEmptyState() throws IOException {
        LocalFilesManager state = new LocalFilesManager(folder.getRoot().toPath());
        assertThat(state.getFiles()).isEmpty();
    }

    @Test
    public void testAddAndGet() throws IOException {
        LocalFilesManager state = new LocalFilesManager(folder.getRoot().toPath());
        state.addLocalFile(id, size);
        testGetFile(state);
    }

    @Test
    public void testAddAndContainsAfterReloading() throws IOException {
        LocalFilesManager storedState = new LocalFilesManager(folder.getRoot().toPath());

        storedState.addLocalFile(id, size);
        testGetFile(storedState);
        storedState.storeToFile();

        LocalFilesManager restoredState = new LocalFilesManager(folder.getRoot().toPath());
        restoredState.restoreFromFile();

        testGetFile(restoredState);
    }

    private void testGetFile(LocalFilesManager state) {
        LocalFileReference fileState = state.getFileState(id);
        assertThat(fileState.getFileId()).isEqualTo(id);
        assertThat(fileState.getNumberOfParts()).isEqualTo(1);
    }

}
