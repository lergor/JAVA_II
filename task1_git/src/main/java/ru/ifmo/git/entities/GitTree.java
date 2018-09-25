package ru.ifmo.git.entities;

import java.io.IOException;
import java.nio.file.*;

public class GitTree {

    private Path rootDir;
    private Path gitDir;
    private Path logDir;
    private Path storageDir;
    private Path indexDir;
    private Path headFile;

    public GitTree() {
        setRepository(cwd());
    }

    public GitTree(Path repository) {
        setRepository(repository);
    }

    private void setRepository(Path repository) {
        rootDir = repository;
        gitDir = rootDir.resolve(".m_git");
        headFile = gitDir.resolve("HEAD");
        logDir = gitDir.resolve("log");
        storageDir = gitDir.resolve("storage");
        indexDir = gitDir.resolve("index");
    }

    public void createGitTree() throws IOException {
        gitDir = Files.createDirectory(rootDir.resolve(".m_git"));
        headFile = Files.createFile(gitDir.resolve("HEAD"));
        logDir = Files.createDirectory(gitDir.resolve("log"));
        storageDir = Files.createDirectory(gitDir.resolve("storage"));
        indexDir = Files.createDirectory(gitDir.resolve("index"));
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
        return gitDir;
    }
    
    public boolean exists() {
        return Files.exists(gitDir);
    }

    public static Path cwd() {
        return Paths.get(".").toAbsolutePath().normalize();
    }
}
