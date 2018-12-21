package ru.ifmo.torrent.client;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.ifmo.torrent.client.state.ClientState;
import ru.ifmo.torrent.client.state.LocalFileState;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


public class ClientStateTest {

    private static final long size = 17;
    private static final int id = 0;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testEmptyState() {
        ClientState state = new ClientState();
        assertThat(state.getFiles()).isEmpty();
    }

    @Test
    public void testAddAndGet() throws IOException {
        ClientState state = new ClientState();
        state.addLocalFile(id, size);
        testGetFile(state);
    }

    @Test
    public void testAddAndContainsAfterReloading() throws IOException {
        ClientState storedState = new ClientState();

        storedState.addLocalFile(id, size);
        testGetFile(storedState);
        Path metaFile = folder.newFile().toPath();
        storedState.storeToFile(metaFile);

        ClientState restoredState = new ClientState();
        restoredState.restoreFromFile(metaFile);

        testGetFile(restoredState);
    }

    private void testGetFile(ClientState state) {
        LocalFileState fileState = state.getFileState(id);
        assertThat(fileState.getFileId()).isEqualTo(id);
        assertThat(fileState.getNumberOfParts()).isEqualTo(1);
    }

}
