package ru.ifmo.torrent.tracker.state;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.ifmo.torrent.tracker.TrackerConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TrackerStateTest {

    private final List<FileInfo> files = Arrays.asList(
            new FileInfo(0, "file_1", 1),
            new FileInfo(1, "file_2", 100),
            new FileInfo(2, "file_3", 17)
    );

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path createMetaFile() throws IOException {
        return folder.newFile(TrackerConfig.TRACKER_STATE_FILE).toPath();
    }

    @Test
    public void testStoringAndRestoringState() throws IOException {
        TrackerState storedState = new TrackerState();
        files.forEach(f -> storedState.addFile(f.name(), f.size()));
        Path file = createMetaFile();
        storedState.storeToFile(file);

        TrackerState restoredState = new TrackerState();
        restoredState.restoreFromFile(file);
        List<FileInfo> restoredFiles = restoredState.getAvailableFiles();

        assertThat(files.size()).isEqualTo(restoredFiles.size());
        for (int i = 0; i < files.size(); i++) {
            assertThat(restoredFiles.get(i).fileID()).isEqualTo(files.get(i).fileID());
            assertThat(restoredFiles.get(i).name()).isEqualTo(files.get(i).name());
            assertThat(restoredFiles.get(i).size()).isEqualTo(files.get(i).size());
        }
    }

    @Test
    public void addAndContainsFileTest() {
        TrackerState state = new TrackerState();
        assertTrue(state.getAvailableFiles().isEmpty());
        String fileName = "kek";
        long fileSize = 17;
        int ID = state.addFile(fileName, fileSize);
        assertThat(state.getAvailableFiles().size()).isEqualTo(1);
        FileInfo addedFile = state.getAvailableFiles().get(0);
        assertThat(addedFile.fileID()).isEqualTo(ID);
        assertThat(addedFile.name()).isEqualTo(fileName);
        assertThat(addedFile.size()).isEqualTo(fileSize);
    }

}
