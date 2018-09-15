package ru.ifmo.git.util;

import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class Utils {

    void checkGitExists() throws  GitException {
        if(!getGitDirectory().exists()) {
            throw new GitException("fatal: Not a git repository: .m_git");
        }
    }

    void checkArguments(List<String> args) throws GitException {
        if(args.size() > 0) {
            String commandName = this.getClass().getSimpleName().toLowerCase();
            throw new GitException("m_git: " + commandName + ": wrong arguments number");
        }
    }

    File getGitDirectory() {
        return new File(getGitPath());
    }

    String getGitPath() {
        return Paths.get(".").toAbsolutePath().normalize().toString() + "/.m_git";
    }

    BranchInfo getHead() throws GitException {
        File headFile = new File(getGitPath() + "/HEAD");
        String headJson = "";
        try(FileInputStream inputStream = new FileInputStream(getGitPath() + "/HEAD")) {
            headJson = IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new GitException("error while reading HEAD");
        }
        return new GsonBuilder().create().fromJson(headJson, BranchInfo.class);
    }

}
