package ru.ifmo.torrent.client;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.ifmo.torrent.client.state.FileManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FileManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testStoreSplittedAndThenReading() throws IOException {
        Path file = folder.newFile().toPath();
        String content = "content";
        FileUtils.writeStringToFile(file.toFile(), content);
        FileManager fileManager = new FileManager(folder.getRoot().toPath());

        int id = 0;
        fileManager.storeSplitted(id, file);
        String storedContent = IOUtils.toString(fileManager.getForReading(id,0));
        assertThat(storedContent).isEqualTo(content);
    }

    @Test
    public void testMergeParts() throws IOException {
        Path fileDir = folder.newFolder("0").toPath();
        FileManager fileManager = new FileManager(folder.getRoot().toPath());
        List<Integer> partsNum = Arrays.asList(0, 1, 2);
        List<Path> files = Arrays.asList(
            Files.createFile(fileDir.resolve("0")),
            Files.createFile(fileDir.resolve("1")),
            Files.createFile(fileDir.resolve("2"))
        );
        List<String> contents = Arrays.asList("content1", "content2", "content3");

        for (int i = 0; i < contents.size(); i++) {
            FileUtils.writeStringToFile(files.get(i).toFile(), contents.get(i));
        }

        Path mergedFile = folder.newFile().toPath();
        fileManager.mergeSplitted(0, mergedFile);

        String storedContent = FileUtils.readFileToString(mergedFile.toFile());
        assertThat(storedContent).isEqualTo("content1content2content3");
    }
}
