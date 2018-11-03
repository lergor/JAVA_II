package ru.ifmo.git;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.ifmo.git.entities.GitFileManager;
import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.structs.CommitInfo;
import ru.ifmo.git.structs.HeadInfo;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.ExitStatus;
import ru.ifmo.git.util.GitException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertFalse;
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
    public void mergeFastForward() throws GitException, IOException {
        String branch = createRepositoryWithBranch(folder, true);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        String branchContent = "new content!";
        String masterContent = "content 3!";
        Path file = manager.structure().repo().resolve("lol");

        isSuccessResultOf(manager.checkout("master"));

        String currentContent = FileUtils.readFileToString(file.toFile());
        assertThat(currentContent).isEqualTo(masterContent);

        CommandResult result = manager.merge(branch);
        assertThat(result.getStatus()).isEqualTo(ExitStatus.SUCCESS);
        assertThat(result.getMessage().read()).contains("Fast forward");

        List<CommitInfo> history = manager.logger().getHistory();
        assertThat(history.get(0).message).isEqualTo("fast forward merge branch '" + branch +
                "' to branch 'master'");
        currentContent = FileUtils.readFileToString(file.toFile());
        assertThat(currentContent).isEqualTo(branchContent);

    }

    @Test
    public void mergeWithoutConflicts() throws GitException, IOException {
        String branch = createRepositoryWithBranch(folder, false);
        GitManager manager = new GitManager(folder.getRoot().toPath());
        Path newFileInBranch = manager.structure().repo().resolve("new_file");

        isSuccessResultOf(manager.checkout("master"));

        String newFileName = "new_file_2";
        Path newFileInMaster = manager.structure().repo().resolve(newFileName);
        Files.createFile(newFileInMaster);
        FileUtils.writeStringToFile(newFileInMaster.toFile(), "new file!");

        isSuccessResultOf(manager.add(Collections.singletonList(newFileInMaster)));
        isSuccessResultOf(manager.commit("add new file"));

        assertTrue(Files.exists(newFileInMaster));
        assertTrue(Files.notExists(newFileInBranch));

        String notChangedContent = "content 3!";
        Path file = manager.structure().repo().resolve("lol");

        String currentContent = FileUtils.readFileToString(file.toFile());
        assertThat(currentContent).isEqualTo(notChangedContent);

        isSuccessResultOf(manager.merge(branch));

        currentContent = FileUtils.readFileToString(file.toFile());
        assertThat(currentContent).isEqualTo(notChangedContent);

        assertTrue(Files.exists(newFileInMaster));
        assertTrue(Files.exists(newFileInBranch));

        assertThat(manager.logger().getHistory().get(0).message).contains("branch '" + branch +
                "' merged to branch 'master'");

    }

    @Test
    public void mergeWithConflicts() throws GitException, IOException {
        String branch = createRepoBeforeMergeWithConflicts(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        String branchContent = "new content!";
        String masterContent = "also new content!";
        String sep = System.lineSeparator();
        Path file = manager.structure().repo().resolve("lol");

        CommandResult result = manager.merge(branch);
        assertThat(result.getStatus()).isEqualTo(ExitStatus.FAILURE);
        String message = result.getMessage().read();

        assertThat(message).contains("Cannot merge");
        assertThat(message).contains("CONFLICT (content):");
        assertThat(message).contains("lol");
        assertThat(message).contains("Automatic merge failed; fix conflicts and then run merge again");

        HeadInfo headInfo = manager.logger().getHeadInfo();
        assertTrue(headInfo.merging());
        assertTrue(headInfo.mergeConflictFlag());
        assertThat(headInfo.getConflictingFilesAsString()
                .replace(sep, "")).isEqualTo("\tlol");

        List<String> conflictingContent = Arrays.asList(FileUtils.readFileToString(file.toFile()).split(sep));

        assertThat(conflictingContent.get(0)).isEqualTo("<<<<<<< HEAD");
        assertThat(conflictingContent.get(1)).isEqualTo(masterContent);
        assertThat(conflictingContent.get(2)).isEqualTo("=======");
        assertThat(conflictingContent.get(3)).isEqualTo(branchContent);
        assertThat(conflictingContent.get(4)).isEqualTo(">>>>>>> " + branch);
    }

    @Test
    public void resolveMergeWithConflicts() throws GitException, IOException {
        String branch = createRepoBeforeMergeWithConflicts(folder);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        String masterContent = "also new content!";
        String sep = System.lineSeparator();
        Path file = manager.structure().repo().resolve("lol");

        isFailResultOf(manager.merge(branch));

        CommandResult result = manager.merge(branch);
        assertThat(result.getStatus()).isEqualTo(ExitStatus.ERROR);
        String message = result.getMessage().read();
        assertThat(message).isEqualTo("fatal: You have not concluded your merge (MERGE_HEAD exists)." +
                sep + "Please, commit your changes before you merge.");

        HeadInfo headInfo = manager.logger().getHeadInfo();
        assertTrue(headInfo.merging());
        assertTrue(headInfo.mergeConflictFlag());
        assertThat(headInfo.getConflictingFilesAsString()
                .replace(sep, "")).isEqualTo("\tlol");

        FileUtils.writeStringToFile(file.toFile(), masterContent);

        isSuccessResultOf(manager.add(Collections.singletonList(file)));

        headInfo = manager.logger().getHeadInfo();
        assertTrue(headInfo.merging());
        assertFalse(headInfo.mergeConflictFlag());
        assertTrue(headInfo.getConflictingFiles().isEmpty());

        isSuccessResultOf(manager.commit("merge"));

        headInfo = manager.logger().getHeadInfo();
        assertFalse(headInfo.merging());
    }

}
