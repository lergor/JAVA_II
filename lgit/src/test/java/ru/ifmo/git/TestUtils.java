package ru.ifmo.git;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import ru.ifmo.git.entities.GitManager;
import ru.ifmo.git.util.CommandResult;
import ru.ifmo.git.util.ExitStatus;
import ru.ifmo.git.util.GitException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TestUtils {

    static final List<Path> files = new ArrayList<>();
    static final List<String> repoStructure = new ArrayList<>();
    static final String branchName = "kek_branch";


    static void isSuccessResultOf(CommandResult result) {
        assertThat(result.getStatus()).isEqualByComparingTo(ExitStatus.SUCCESS);
    }

    static void isFailResultOf(CommandResult result) {
        assertThat(result.getStatus()).isEqualByComparingTo(ExitStatus.FAILURE);
    }

    static void createRepository(TemporaryFolder folder) throws IOException {
        files.clear();

        Path file = folder.newFolder("kek").toPath();
        files.add(file);
        repoStructure.add("kek");

        file = folder.newFile("lol").toPath();
        FileUtils.writeStringToFile(file.toFile(), "lol content");
        files.add(file);
        repoStructure.add("lol");

        file = folder.newFile("kek/meme").toPath();
        FileUtils.writeStringToFile(file.toFile(), "meme content");
        files.add(file);
        repoStructure.add("kek/meme");
    }

    static void createRepositoryWithHistory(TemporaryFolder folder) throws GitException, IOException {
        createRepository(folder);

        GitManager manager = new GitManager(folder.getRoot().toPath());
        Path changedFile = folder.getRoot().toPath().resolve("lol");

        isSuccessResultOf(manager.init());

        isSuccessResultOf(manager.add(Collections.singletonList(changedFile)));
        isSuccessResultOf(manager.commit("1"));

        FileUtils.writeStringToFile(changedFile.toFile(), "content 2!");

        isSuccessResultOf(manager.add(Collections.singletonList(changedFile)));
        isSuccessResultOf(manager.commit("2"));

        FileUtils.writeStringToFile(changedFile.toFile(), "content 3!");

        isSuccessResultOf(manager.add(Collections.singletonList(changedFile)));
        isSuccessResultOf(manager.commit("3"));

        assertThat(manager.logger().getHistory().size()).isEqualTo(3);
    }

    static List<String> makeStatusRepoToIndex(TemporaryFolder folder) throws IOException, GitException {
        GitManager manager = new GitManager(folder.getRoot().toPath());
        String modifiedFile = repoStructure.get(1);
        FileUtils.writeStringToFile(manager.structure().repo()
                .resolve(modifiedFile).toFile(), "new content!");

        String deletedFile = repoStructure.get(2);
        Files.deleteIfExists(manager.structure().repo().resolve(deletedFile));

        String newFile = "new_file";
        Files.createFile(manager.structure().repo().resolve(newFile));
        return Arrays.asList(newFile, modifiedFile, deletedFile);
    }

    static String createRepositoryWithBranch(TemporaryFolder folder, boolean withConflict)
            throws GitException, IOException {
        createRepositoryWithHistory(folder);

        GitManager manager = new GitManager(folder.getRoot().toPath());
        isSuccessResultOf(manager.checkoutNewBranch(branchName));

        List<String> statusFiles = makeStatusRepoToIndex(folder);
        List<Path> paths = statusFiles.stream()
                .map(f -> manager.structure().repo().resolve(f)).collect(Collectors.toList());
        if (!withConflict) {
            isSuccessResultOf(manager.checkout(Arrays.asList(paths.get(1), paths.get(2))));
            paths = Collections.singletonList(paths.get(0));
        }
        isSuccessResultOf(manager.add(paths));
        isSuccessResultOf(manager.commit("commit new branch"));

        return branchName;
    }

    static String createRepoBeforeMergeWithConflicts(TemporaryFolder folder) throws GitException, IOException {
        String branch = createRepositoryWithBranch(folder, true);
        GitManager manager = new GitManager(folder.getRoot().toPath());

        Path conflictingFile = manager.structure().repo().resolve("lol");

        isSuccessResultOf(manager.checkout("master"));

        FileUtils.writeStringToFile(conflictingFile.toFile(), "also new content!");

        isSuccessResultOf(manager.add(Collections.singletonList(conflictingFile)));
        isSuccessResultOf(manager.commit("make conflict"));

        return branch;
    }

}
