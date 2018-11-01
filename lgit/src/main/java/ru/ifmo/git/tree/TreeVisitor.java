package ru.ifmo.git.tree;

import ru.ifmo.git.util.GitException;

import java.io.IOException;
import java.util.List;

public interface TreeVisitor {

    void visit(TreeFile tree) throws IOException, GitException;

    void visit(TreeDirectory tree) throws IOException, GitException;

    default void visit(List<Tree> trees) throws IOException, GitException {
        for (Tree tree : trees) {
            tree.accept(this);
        }
    }
}
