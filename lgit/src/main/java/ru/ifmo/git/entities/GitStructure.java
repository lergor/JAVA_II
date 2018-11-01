package ru.ifmo.git.entities;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

public class GitStructure {

    private Path rootDir;
    private Path metaDir;
    private Path logDir;
    private Path storageDir;
    private Path indexDir;
    private Path headFile;
    private Path usageInfo;

    public GitStructure(Path repository) {
        setRepository(repository);
    }

    private void setRepository(Path repository) {
        rootDir = repository;
        metaDir = rootDir.resolve(".l_git");
        headFile = metaDir.resolve("HEAD");
        logDir = metaDir.resolve("log");
        storageDir = metaDir.resolve("storage");
        indexDir = metaDir.resolve("index");
        usageInfo = storageDir.resolve("usages");
    }

    void createGitTree() throws IOException {
        metaDir = Files.createDirectory(rootDir.resolve(".l_git"));
        headFile = Files.createFile(metaDir.resolve("HEAD"));
        logDir = Files.createDirectory(metaDir.resolve("log"));
        Files.createFile(logDir.resolve("master"));
        storageDir = Files.createDirectory(metaDir.resolve("storage"));
        indexDir = Files.createDirectory(metaDir.resolve("index"));
        usageInfo = Files.createFile(storageDir.resolve("usages"));
    }

    public Path repo() {
        return rootDir;
    }

    public Path index() {
        return indexDir;
    }

    Path log() {
        return logDir;
    }

    public Path storage() {
        return storageDir;
    }

    Path head() {
        return headFile;
    }

    Path usageInfo() {
        return usageInfo;
    }

    public boolean exists() {
        return Files.exists(metaDir);
    }

}
