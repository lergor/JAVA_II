package ru.ifmo.git;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.ExitStatus;
import ru.ifmo.git.util.GitException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class TestCommands2 extends TestUtils {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testRemove() throws GitException, IOException {
        createRepositoryWithHistory(folder);

        Path repo = folder.getRoot().toPath();
        GitManager manager = new GitManager(repo);
        String fileToRemove = repoStructure.get(1);

        Path fileInRepo = manager.structure().repo().resolve(fileToRemove);
        Path fileInIndex = manager.structure().index().resolve(fileToRemove);

        assertTrue(Files.exists(fileInRepo));
        assertTrue(Files.exists(fileInIndex));
        isSuccessResultOf(manager.remove(Collections.singletonList(fileInRepo)));
        assertTrue(Files.exists(fileInRepo));
        assertFalse(Files.exists(fileInIndex));
    }

    @Test
    public void testStatusEmptyIndex() throws GitException, IOException {
        GitManager manager = new GitManager(folder.getRoot().toPath());
        isSuccessResultOf(manager.init());
        String status = manager.status(null).getMessage().read();
        assertThat(status.contains("No commits yet"));
    }

    @Test
    public void testStatusEmptyRepo() throws GitException, IOException {
        createRepositoryWithHistory(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());
        String status = manager.status(null).getMessage().read();
        assertThat(status.contains("No changed files"));
    }

    @Test
    public void testStatusRepoToIndex() throws GitException, IOException {
        createRepositoryWithHistory(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());
        List<String> statusFiles = makeStatusRepoToIndex(folder);

        CommandResult result = manager.status(null);
        assertThat(result.getStatus()).isEqualTo(ExitStatus.SUCCESS);
        String status = result.getMessage().read();

        assertTrue(status.contains("Untracked files:"));
        assertTrue(status.contains("new: " + statusFiles.get(0)));
        assertTrue(status.contains("modified: " + statusFiles.get(1)));
        assertTrue(status.contains("deleted: " + statusFiles.get(2)));

        assertFalse(status.contains("Tracked files:"));
    }

    @Test
    public void testStatusIndexToLastCommit() throws GitException, IOException {
        createRepositoryWithHistory(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());
        List<String> statusFiles = makeStatusRepoToIndex(folder);

        String newFileAdded = statusFiles.get(0);
        String modifiedFileAdded = statusFiles.get(1);
        String deletedFileAdded = statusFiles.get(2);

        Path pathNew = manager.structure().repo().resolve(newFileAdded);
        Path pathModified = manager.structure().repo().resolve(modifiedFileAdded);
        Path pathDeleted = manager.structure().repo().resolve(deletedFileAdded);

        isSuccessResultOf(manager.add(Arrays.asList(pathNew, pathModified, pathDeleted)));

        CommandResult result = manager.status(null);
        assertThat(result.getStatus()).isEqualTo(ExitStatus.SUCCESS);
        String status = result.getMessage().read();

        assertTrue(status.contains("Tracked files:"));
        assertTrue(status.contains("new: " + newFileAdded));
        assertTrue(status.contains("modified: " + modifiedFileAdded));
        assertTrue(status.contains("deleted: " + deletedFileAdded));

        assertFalse(status.contains("Untracked files:"));
    }

    @Test
    public void testStatusToCommit() throws GitException, IOException {
        createRepositoryWithHistory(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());
        String hash = manager.logger().getHistory().get(2).hash();
        String modifiedFile = repoStructure.get(1);

        FileUtils.writeStringToFile(
                manager.structure().repo().resolve(modifiedFile).toFile(),
                "new content!");

        CommandResult result = manager.status(hash);
        assertThat(result.getStatus()).isEqualTo(ExitStatus.SUCCESS);
        String status = result.getMessage().read();

        assertTrue(status.contains("Tracked files:"));
        assertTrue(status.contains("modified: " + modifiedFile));

        assertFalse(status.contains("new: "));
        assertFalse(status.contains("deleted: "));
//        assertFalse(status.contains("Untracked files:")); // ???
    }

    @Test
    public void testFullStatus() throws GitException, IOException {
        createRepositoryWithHistory(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());
        List<String> statusFiles = makeStatusRepoToIndex(folder);

        String newFileAdded = statusFiles.get(0);
        String modifiedFileAdded = statusFiles.get(1);
        String deletedFileAdded = statusFiles.get(2);

        Path pathNew = manager.structure().repo().resolve(newFileAdded);
        Path pathModified = manager.structure().repo().resolve(modifiedFileAdded);
        Path pathDeleted = manager.structure().repo().resolve(deletedFileAdded);

        isSuccessResultOf(manager.add(Arrays.asList(pathNew, pathDeleted, pathModified)));

        String modifiedFileInRepo = newFileAdded;
        FileUtils.writeStringToFile(pathNew.toFile(), "new content again!");

        String newFileInRepo = "new_file_2";
        Files.createFile(manager.structure().repo().resolve(newFileInRepo));

        String deletedFileInRepo = modifiedFileAdded;
        Files.deleteIfExists(manager.structure().repo().resolve(deletedFileInRepo));

        CommandResult result = manager.status(null);
        assertThat(result.getStatus()).isEqualTo(ExitStatus.SUCCESS);
        String status = result.getMessage().read();

        assertTrue(status.contains("Tracked files:"));
        assertTrue(status.contains("new: " + newFileAdded));
        assertTrue(status.contains("modified: " + modifiedFileAdded));
        assertTrue(status.contains("deleted: " + deletedFileAdded));

        assertTrue(status.contains("Untracked files:"));
        assertTrue(status.contains("new: " + newFileInRepo));
        assertTrue(status.contains("modified: " + modifiedFileInRepo));
        assertTrue(status.contains("deleted: " + deletedFileInRepo));
    }

    @Test
    public void testCheckoutMinusMinus() throws GitException, IOException {
        createRepositoryWithHistory(folder);

        GitManager manager = new GitManager(folder.getRoot().toPath());
        String changingFile = repoStructure.get(1);
        Path fileInIndex = manager.structure().index().resolve(changingFile);
        Path fileInRepo = manager.structure().repo().resolve(changingFile);

        String commitedContent = FileUtils.readFileToString(fileInIndex.toFile());
        String addedContent;
        String currentContent = FileUtils.readFileToString(fileInRepo.toFile());
        assertThat(currentContent).isEqualTo(commitedContent);

        String newContent = "changed content";
        FileUtils.writeStringToFile(fileInRepo.toFile(), newContent);
        isSuccessResultOf(manager.add(Collections.singletonList(fileInRepo)));

        addedContent = FileUtils.readFileToString(fileInIndex.toFile());
        currentContent = FileUtils.readFileToString(fileInRepo.toFile());
        assertThat(currentContent).isEqualTo(addedContent);
        assertThat(currentContent).isNotEqualTo(commitedContent);

        isSuccessResultOf(manager.checkout(Collections.singletonList(fileInRepo)));
        addedContent = FileUtils.readFileToString(fileInIndex.toFile());
        currentContent = FileUtils.readFileToString(fileInRepo.toFile());
        assertThat(currentContent).isEqualTo(addedContent);
        assertThat(currentContent).isEqualTo(commitedContent);
    }


}