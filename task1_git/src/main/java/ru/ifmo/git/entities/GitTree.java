package ru.ifmo.git.entities;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

public class GitTree {

    private Path rootDir;
    private Path metaDir;
    private Path logDir;
    private Path storageDir;
    private Path indexDir;
    private Path headFile;

    GitTree(Path repository) {
        setRepository(repository);
    }

    private void setRepository(Path repository) {
        rootDir = repository;
        metaDir = rootDir.resolve(".l_git");
        headFile = metaDir.resolve("HEAD");
        logDir = metaDir.resolve("log");
        storageDir = metaDir.resolve("storage");
        indexDir = metaDir.resolve("index");
    }

    void createGitTree() throws IOException {
        metaDir = Files.createDirectory(rootDir.resolve(".l_git"));
        headFile = Files.createFile(metaDir.resolve("HEAD"));
        logDir = Files.createDirectory(metaDir.resolve("log"));
        storageDir = Files.createDirectory(metaDir.resolve("storage"));
        indexDir = Files.createDirectory(metaDir.resolve("index"));
    }

    Path repo() {
        return rootDir;
    }

    Path index() {
        return indexDir;
    }

    Path log() {
        return logDir;
    }

    Path storage() {
        return storageDir;
    }

    Path head() {
        return headFile;
    }

    Path git() {
        return metaDir;
    }

    public boolean exists() {
        return Files.exists(metaDir);
    }

}
