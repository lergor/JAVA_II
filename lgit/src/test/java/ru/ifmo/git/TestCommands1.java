package ru.ifmo.git;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.ifmo.git.entities.GitFileManager;
import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.entities.GitStructure;
import ru.ifmo.git.structs.CommitInfo;
import ru.ifmo.git.structs.HeadInfo;
import ru.ifmo.git.tree.Tree;
import ru.ifmo.git.tree.TreeDirectory;
import ru.ifmo.git.tree.TreeEncoder;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.ExitStatus;
import ru.ifmo.git.util.GitException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ru.ifmo.git.TestUtils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestCommands1 {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testInit() throws GitException {
        Path repo = folder.getRoot().toPath();
        GitStructure structure = new GitStructure(repo);
        assertThat(structure.exists()).isFalse();
        GitManager manager = new GitManager(repo);

        isSuccessResultOf(manager.init());

        assertThat(structure.exists()).isTrue();
    }

    @Test
    public void testAdd() throws IOException, GitException {
        createRepository(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        isSuccessResultOf(manager.init());
        isSuccessResultOf(manager.add(files));

        for (String file : repoStructure) {
            Path indexFile = manager.structure().index().resolve(file);
            assertThat(Files.exists(indexFile));
        }
    }

    @Test
    public void testAddEmptyRepo() throws IOException, GitException {
        GitManager manager = new GitManager(folder.getRoot().toPath());

        isSuccessResultOf(manager.init());
        isSuccessResultOf(manager.add(files));

        List<Path> indexFiles = Files.list(manager.structure().index())
                .collect(Collectors.toList());
        assertThat(indexFiles).containsExactlyElementsOf(Collections.emptyList());
    }

    @Test
    public void testEmptyCommit() throws GitException, IOException {
        createRepository(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        isSuccessResultOf(manager.init());
        isSuccessResultOf(manager.commit("message"));

        List<CommitInfo> history = manager.logger().getHistory();
        assertThat(history.size()).isEqualTo(0);

        Tree commitedTree = Tree.createTree(manager.structure().storage());
        List<Tree> children = ((TreeDirectory) commitedTree).children();
        assertThat(children.size()).isEqualTo(1);
        assertThat(children.get(0).path()).isEqualTo("usages");

    }

    @Test
    public void testAddThenCommit() throws GitException, IOException {
        createRepository(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        isSuccessResultOf(manager.init());
        isSuccessResultOf(manager.add(files));
        isSuccessResultOf(manager.commit("message"));

        List<CommitInfo> history = manager.logger().getHistory();
        assertThat(history.size()).isEqualTo(1);

        HeadInfo headInfo = manager.logger().getHeadInfo();
        assertThat(headInfo.currentHash).isEqualTo(history.get(0).hash);

        Tree commitedTree = new TreeEncoder(manager.structure().storage()).decode(headInfo.currentHash);

        for (String file : repoStructure) {
            assertNotNull(commitedTree.find(file));
        }
    }

    @Test
    public void testLog() throws GitException, IOException {
        createRepositoryWithHistory(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());
        CommandResult result = manager.log(null);
        assertThat(result.getStatus()).isEqualTo(ExitStatus.SUCCESS);

        String log = result.getMessage().read();
        List<CommitInfo> history = manager.logger().getHistory();
        for (CommitInfo i: history) {
            assertTrue(log.contains(i.hash));
            assertTrue(log.contains(i.time));
            assertTrue(log.contains(i.message));
        }
    }

    @Test
    public void testLogWithRevision() throws GitException, IOException {
        createRepositoryWithHistory(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());
        List<CommitInfo> history = manager.logger().getHistory();
        int revision = 0;
        CommandResult result = manager.log(history.get(revision).hash);
        assertThat(result.getStatus()).isEqualTo(ExitStatus.SUCCESS);
        String log = result.getMessage().read();

        for (int i = 0; i < 3; i++) {
            assertThat(log.contains(history.get(i).hash)).isEqualTo(i == revision);
        }
        assertThat(log).isEqualTo(history.get(revision).toString());
    }

    @Test
    public void testReset() throws GitException, IOException {
        createRepositoryWithHistory(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        List<CommitInfo> history = manager.logger().getHistory();
        String commitToReset = history.get(1).hash;
        String commitToDelete = history.get(0).hash;
        Path commitFileToDelete = manager.structure().storage()
                .resolve(GitFileManager.pathInStorage(manager.structure().storage(), commitToDelete));
        Path file = manager.structure().index().resolve("lol");
        String content = FileUtils.readFileToString(file.toFile());

        assertThat(history.size()).isEqualTo(3);
        assertThat(manager.logger().getHeadInfo().currentHash).isEqualTo(commitToDelete);
        assertTrue(Files.exists(commitFileToDelete));
        assertThat(content).isEqualTo("content 3!");

        isSuccessResultOf(manager.reset(commitToReset));

        assertFalse(Files.exists(commitFileToDelete));
        assertThat(manager.logger().getHeadInfo().currentHash).isEqualTo(commitToReset);

        content = FileUtils.readFileToString(file.toFile());
        assertThat(content).isEqualTo("content 2!");
        history = manager.logger().getHistory();
        assertThat(history.size()).isEqualTo(2);
    }

    @Test
    public void testCheckout() throws GitException, IOException {
        createRepositoryWithHistory(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        List<CommitInfo> history = manager.logger().getHistory();
        String currentCommit = history.get(0).hash;
        String commitToCheckout = history.get(2).hash;
        String currentContent = "content 3!";
        String checkoutContent = "lol content";

        Path file = manager.structure().index().resolve("lol");
        String content = FileUtils.readFileToString(file.toFile());

        assertThat(manager.logger().getHeadInfo().currentHash).isEqualTo(currentCommit);
        assertThat(content).isEqualTo(currentContent);

        isSuccessResultOf(manager.checkout(commitToCheckout));
        assertThat(manager.logger().getHeadInfo().currentHash).isEqualTo(commitToCheckout);

        content = FileUtils.readFileToString(file.toFile());
        assertThat(content).isEqualTo(checkoutContent);
    }

    @Test
    public void doubleCheckout() throws GitException, IOException {
        createRepositoryWithHistory(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        List<CommitInfo> history = manager.logger().getHistory();
        String currentCommit = history.get(0).hash;
        String commitToCheckout = history.get(2).hash;
        String currentContent = "content 3!";
        String checkoutContent = "lol content";

        Path file = manager.structure().index().resolve("lol");
        String content = FileUtils.readFileToString(file.toFile());

        assertThat(manager.logger().getHeadInfo().currentHash).isEqualTo(currentCommit);
        assertThat(content).isEqualTo(currentContent);

        isSuccessResultOf(manager.checkout(commitToCheckout));
        assertThat(manager.logger().getHeadInfo().currentHash).isEqualTo(commitToCheckout);

        content = FileUtils.readFileToString(file.toFile());
        assertThat(content).isEqualTo(checkoutContent);

        isSuccessResultOf(manager.checkout(currentCommit));
        assertThat(manager.logger().getHeadInfo().currentHash).isEqualTo(currentCommit);

        content = FileUtils.readFileToString(file.toFile());
        assertThat(content).isEqualTo(currentContent);
    }

}