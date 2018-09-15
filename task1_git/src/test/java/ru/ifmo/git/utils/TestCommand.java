package ru.ifmo.git.utils;

import org.junit.Test;
import ru.ifmo.git.util.*;

import static junit.framework.TestCase.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestCommand {

    private Command command = new Command() {
        @Override
        public CommandResult execute(List<String> args) throws GitException {
            return null;
        }
    };

    @Test
    public void GetGitPathTest() {
        String path = command.getGitPath();
        assertEquals(Paths.get(".").toAbsolutePath().normalize().toString() + "/.m_git", path);
    }

    @Test
    public void GetGitDirectoryTest() {
        File gitDir = command.getGitDirectory();
        assertEquals(command.getGitPath(), gitDir.getAbsolutePath());
    }

    @Test(expected = GitException.class)
    public void CheckArgumentsThrowsTest() {
        command.correctArgs(Arrays.asList("kek", "kek"));
    }

    @Test
    public void CheckArgumentsNotThrowTest() {
        command.correctArgs(Collections.emptyList());
    }

    @Test
    public void CheckGitExistsNotThrowTest() {
        File gitDir = command.getGitDirectory();
        if(!gitDir.exists()) {
            gitDir.mkdir();
        }
        command.repositoryExists();
    }

    @Test
    public void GetHeadTest() throws GitException, IOException {
//        File headFile = new File(command.getGitPath() + "/HEAD");
//        String json = "{\"branchName\":\"master\"," +
//                        "\"headHash\":\"812932983\"," +
//                        "\"historyFilePath\":\"info/hist_master\"," +
//                        "\"logFilePath\":\"logs/master\"," +
//                        "\"storagePath\":\"storage/812932983\"}";
//
//        boolean k = headFile.exists();
//        if(!k) {
//            System.out.println(headFile.getAbsolutePath());
//            k = headFile.createNewFile() &&
//                headFile.setReadable(true) &&
//                headFile.setWritable(true);
//        }
//        if(k) {
//            try(FileWriter writer = new FileWriter(headFile)) {
//                writer.write(json);
//                writer.flush();
//                writer.close();
//            } catch (IOException ignored) {
//                System.out.println("catch");
//            }
//        }
//
//
//        BranchInfo info = command.getHead();
//        assertEquals("master", info.branchName);
//        assertEquals("812932983", info.headHash);
//        assertEquals("/info/hist_master", info.historyFilePath);
//        assertEquals("/logs/master", info.logFilePath);
//        assertEquals("/storage/812932983", info.branchName);
//        assertTrue(headFile.delete());
    }

    @Test(expected = GitException.class)
    public void CheckGitExistsThrowsTest() throws GitException {
        File gitDir = command.getGitDirectory();
        if(gitDir.exists()) {
            gitDir.delete();
        }
        command.repositoryExists();
    }
}
