package ru.ifmo.torrent;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import ru.ifmo.torrent.tracker.Tracker;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TorrentTest {

    @Rule
    public TemporaryFolder folder1 = new TemporaryFolder();

    @Rule
    public TemporaryFolder folder2 = new TemporaryFolder();

    public void testTorrent() {
    }

}
