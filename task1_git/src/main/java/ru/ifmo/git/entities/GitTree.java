package ru.ifmo.git.entities;

import java.io.IOException;
import java.nio.file.*;

public class GitTree {

    private Path rootDir;
    private Path metaDir;
    private Path logDir;
    private Path storageDir;
    private Path indexDir;
    private Path headFile;

    public GitTree(Path repository) {
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

    public void createGitTree() throws IOException {
        metaDir = Files.createDirectory(rootDir.resolve(".l_git"));
        headFile = Files.createFile(metaDir.resolve("HEAD"));
        logDir = Files.createDirectory(metaDir.resolve("log"));
        storageDir = Files.createDirectory(metaDir.resolve("storage"));
        indexDir = Files.createDirectory(metaDir.resolve("index"));
    }

    public Path repo() {
        return rootDir;
    }

    public Path index() {
        return indexDir;
    }

    public Path log() {
        return logDir;
    }

    public Path storage() {
        return storageDir;
    }

    public Path head() {
        return headFile;
    }

    public Path git() {
        return metaDir;
    }
    
    public boolean exists() {
        return Files.exists(metaDir);
    }
}
