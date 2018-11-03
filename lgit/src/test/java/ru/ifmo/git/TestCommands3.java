package ru.ifmo.git;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.ifmo.git.entities.GitFileManager;
import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.structs.CommitInfo;
import ru.ifmo.git.tree.Tree;
import ru.ifmo.git.tree.TreeEncoder;
import ru.ifmo.git.util.GitException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static ru.ifmo.git.TestUtils.*;

public class TestCommands3 {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final String branchName = "kek_branch";

    @Test
    public void newBranch() throws GitException, IOException {
        createRepositoryWithHistory(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        isSuccessResultOf(manager.newBranch(branchName));
        String allBranches = manager.listBranches().getMessage().read();
        assertThat(allBranches).contains(branchName);
    }

    @Test
    public void checkoutNewBranch() throws GitException, IOException {
        createRepositoryWithHistory(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        isSuccessResultOf(manager.checkoutNewBranch(branchName));
        String allBranches = manager.listBranches().getMessage().read();
        assertThat(allBranches).contains(branchName);
        assertThat(manager.logger().getHeadInfo().branch()).isEqualTo(branchName);
    }

    @Test
    public void checkoutBranch() throws GitException, IOException {
        String branch = createRepositoryWithBranch(folder, true);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        String branchContent = "new content!";
        String masterContent = "content 3!";
        Path file = manager.structure().repo().resolve("lol");

        String currentContent = FileUtils.readFileToString(file.toFile());
        assertThat(manager.logger().getHeadInfo().branch()).isEqualTo(branch);
        assertThat(currentContent).isEqualTo(branchContent);

        isSuccessResultOf(manager.checkout("master"));
        currentContent = FileUtils.readFileToString(file.toFile());

        assertThat(manager.logger().getHeadInfo().branch()).isEqualTo("master");
        assertThat(currentContent).isEqualTo(masterContent);
    }

    @Test
    public void deleteBranch() throws GitException, IOException {
        String branch = createRepositoryWithBranch(folder, true);
        GitManager manager = new GitManager(folder.getRoot().toPath());
        String branchCommit = manager.logger().getBranchCommits(branch).get(0).hash;
        String branchTree = manager.logger().getBranchCommits(branch).get(0).treeHash;

        Path branchCommitFile = manager.structure().storage()
                .resolve(GitFileManager.pathInStorage(manager.structure().storage(), branchCommit));

        Path branchTreeFile = manager.structure().storage()
                .resolve(GitFileManager.pathInStorage(manager.structure().storage(), branchTree));

        String allBranches = manager.listBranches().getMessage().read();
        assertThat(allBranches).contains(branchName);
        assertTrue(Files.exists(branchCommitFile));
        assertTrue(Files.exists(branchTreeFile));

        isSuccessResultOf(manager.checkout("master"));
        isSuccessResultOf(manager.deleteBranch(branch));

        allBranches = manager.listBranches().getMessage().read();
        assertThat(allBranches).doesNotContain(branchName);
        assertTrue(Files.notExists(branchCommitFile));
        assertTrue(Files.notExists(branchTreeFile));
    }

    @Test
    public void mergeWithoutConflicts() throws GitException, IOException {
        String branch = createRepositoryWithBranch(folder, false);
        GitManager manager = new GitManager(folder.getRoot().toPath());
        isSuccessResultOf(manager.checkout("master"));
        manager.merge(branch).print();
//        isSuccessResultOf(manager.merge(branch));

        List<CommitInfo> history = manager.logger().getHistory();
        assertThat(history.get(0).message).isEqualTo("branch '" + branch
                + "' merged to branch 'master'");

    }

    @Test
    public void mergeWithConflicts() throws GitException, IOException {

    }

    @Test
    public void resolveMergeWithConflicts() throws GitException, IOException {

    }

}
