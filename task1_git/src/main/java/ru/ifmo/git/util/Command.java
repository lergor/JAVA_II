package ru.ifmo.git.util;

import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
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

        default HeadInfo getHeadInfo() throws GitException {
            String headJson;
            try(FileInputStream inputStream = new FileInputStream(getGitPath() + "/HEAD")) {
                headJson = IOUtils.toString(inputStream);
            } catch (IOException e) {
                throw new GitException("error while reading HEAD\n");
            }
            return new GsonBuilder().create().fromJson(headJson, HeadInfo.class);
        }

        default String getCWD() {
            return Paths.get(".").toAbsolutePath().normalize().toString();
        }

    default CommandResult copyAllToDir(List<String> args, String destDir) {
        File destination = new File(getGitPath() + "/" + destDir);
        if(destination.exists() || (!destination.exists() && destination.mkdirs())) {
            for (String fileName: args) {
                if(fileName.startsWith("./")) {
                    fileName = fileName.substring(1, fileName.length());
                }
                if(fileName.endsWith("/")) {
                    fileName = fileName.substring(0, fileName.length() - 1);
                }
                try {
                    copyTo(fileName, destDir);
                } catch (GitException e) {
                    return new CommandResult(ExitStatus.ERROR, e.getMessage());
                }
            }
        }
        return new CommandResult(ExitStatus.SUCCESS);

    }

    default void copyTo(String source, String destDir) throws GitException {
        File sourceFile = new File(getCWD() + source);
        File destinationFile = new File(getGitPath() + "/" + destDir + "/" + source);
        if(!sourceFile.isHidden()) {
            try {
                if(sourceFile.isFile()) {
                    FileUtils.copyFileToDirectory(sourceFile, destinationFile);
                } else if(sourceFile.isDirectory()) {
                    File[] files = sourceFile.listFiles(pathname -> !pathname.isHidden());
                    if(files != null) {
                        for (File file: files) {
                            String shortPath = "/" + sourceFile.getName() + "/" + file.getName();
                            copyTo(shortPath, destDir);
                        }
                    }
                }
            } catch (IOException e) {
                String errorMsg = "error while moving " + source + " into " + getGitPath() + destDir + "\n";
                throw new GitException(errorMsg);
            }
        }
    }

    default boolean createFileWithContent(String fileName, String content) {
        return checkAndCreateFile(fileName) && writeToFile(fileName, content, false);
    }

    default boolean checkAndCreateDir(String dirName) {
        File dir = new File(getGitPath() + "/" + dirName);
        return dir.exists() || dir.mkdirs();
    }

    default boolean checkAndCreateFile(String fileName) {
        File file = new File(getGitPath() + "/" + fileName);
        if(!file.exists()) {
            try {
                if(file.createNewFile()) {
                    return file.setReadable(true) && file.setWritable(true);
                }
            } catch (IOException e) {
                return false;
            }
        }
        return file.canRead() && file.canWrite();
    }

    default boolean writeToFile(String fileName, String content, boolean append) {
        File file = new File(getGitPath() + "/" + fileName);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
            writer.write((append ? "\n" : "") + content);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

        CommandResult execute(List<String> args) throws GitException;

    }
