package ru.ifmo.git.entities;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GitAssembly {

    private GitTree gitTree;
    private GitClerk gitClerk;
    private GitFileKeeper gitFileKeeper;
    private GitCryptographer gitCrypto;

    public GitAssembly(Path cwd) {
        gitTree = new GitTree(cwd);
        gitClerk = new GitClerk(gitTree);
        gitFileKeeper = new GitFileKeeper(gitTree);
        gitCrypto = new GitCryptographer(gitTree);
    }

    public GitClerk clerk() {
        return gitClerk;
    }

    public GitCryptographer crypto() {
        return gitCrypto;
    }

    public GitFileKeeper fileKeeper() {
        return gitFileKeeper;
    }

    public GitTree tree() {
        return gitTree;
    }

    public static Path cwd() {
        return Paths.get(".").toAbsolutePath().normalize();
    }
}
