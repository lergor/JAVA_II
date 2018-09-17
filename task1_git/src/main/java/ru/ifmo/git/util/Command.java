package ru.ifmo.git.util;

import java.io.File;
import java.util.*;

public interface Command {

    default boolean repositoryExists() {
        return new File(GitTree.repo()).exists();
    }

    boolean correctArgs(Map<String, Object> args);

    default void checkRepoAndArgs(Map<String, Object> args) throws GitException {
        if (!repositoryExists()) {
            throw new GitException("fatal: Not a git repository: .m_git\n");
        }
        if (!correctArgs(args)) {
            throw new GitException("wrong arguments\n");
        }
    }

    CommandResult execute(Map<String, Object> args) throws GitException;

}
