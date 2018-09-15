package ru.ifmo.git.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public interface Command {

    default boolean repositoryExists() {
        return getGitDirectory().exists();
    }

    default boolean correctArgs(List<String> args) {
        return args == null || args.isEmpty();
    }


        default File getGitDirectory() {
            return new File(getGitPath());
        }

        default String getGitPath() {
            return getCWD() + "/.m_git";
        }

        default BranchInfo getHead() throws GitException {
            String headJson = "";
            try(FileInputStream inputStream = new FileInputStream(getGitPath() + "/HEAD")) {
                headJson = IOUtils.toString(inputStream);
            } catch (IOException e) {
                throw new GitException("error while reading HEAD");
            }
            return new GsonBuilder().create().fromJson(headJson, BranchInfo.class);
        }

        default String getCWD() {
            return Paths.get(".").toAbsolutePath().normalize().toString();
        }

        CommandResult execute(List<String> args) throws GitException;

    }
