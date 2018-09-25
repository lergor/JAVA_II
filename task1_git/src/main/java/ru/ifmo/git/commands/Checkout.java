package ru.ifmo.git.commands;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Checkout implements GitCommand {

    private Path commitFile;
    private GitTree gitTree;
    private GitClerk gitClerk;
    private GitFileKeeper gitFileKeeper;
    private GitCryptographer gitCrypto;

    public Checkout() {
        initEntities(GitTree.cwd());
    }

    public Checkout(Path cwd) {
        initEntities(cwd);
    }

    private void initEntities(Path cwd) {
        gitTree = new GitTree(cwd);
        gitClerk = new GitClerk(gitTree);
        gitFileKeeper = new GitFileKeeper(gitTree);
        gitCrypto = new GitCryptographer(gitTree);
    }

    @Override
    public boolean correctArgs(Map<String, Object> args) throws GitException {
        String commitHash = (String) args.get("<commitHash>");
        if(commitHash.length() > 6) {
            try {
                Optional<Path> commit = gitFileKeeper.findFileInStorage(commitHash);
                if(commit.isPresent()) {
                    commitFile = commit.get();
                    return true;
                }
            } catch (IOException e) {
                throw new GitException(e.getMessage());
            }
        }
        return false;
    }

    @Override
    public CommandResult doWork(Map<String, Object> args) throws GitException {
        if (!gitTree.exists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: not a m_git repository");
        }
        try {
            List<FileReference> references = gitCrypto.formDecodeReferences(commitFile);
            for (FileReference i : references) {
                System.out.println(i.name);
            }
            GitFileKeeper.clearDirectory(gitTree.repo());
            gitFileKeeper.restoreCommit(references, gitTree.repo());
            changeHeadInfo();
        } catch (IOException e) {
            throw new GitException(e.getMessage());
        }
        return new CommandResult(ExitStatus.SUCCESS, "checkout: done!");

    }

    private void changeHeadInfo() throws GitException {
        HeadInfo headInfo = gitClerk.getHeadInfo();
        headInfo.moveCurrent(commitFile.toFile().getName());
        gitClerk.changeHeadInfo(headInfo);
    }

}
