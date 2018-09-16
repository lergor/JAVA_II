package ru.ifmo.git.util;

import java.util.*;

public interface Command {

    default boolean repositoryExists() {
        return GitUtils.getGitDirectory().exists();
    }

    boolean correctArgs(List<String> args);

    default void checkRepoAndArgs(List<String> args) throws GitException {
        if(!repositoryExists()) {
            throw new GitException("fatal: Not a git repository: .m_git\n");
        }
        if(!correctArgs(args)) {
            throw  new GitException("wrong arguments\n");
        }
    }

    CommandResult execute(List<String> args) throws GitException;

}
