package ru.ifmo.git.tree;

import java.io.IOException;
import java.util.List;

public interface TreeVisitor {

    void visit(TreeFile tree) throws IOException;

    void visit(TreeDirectory tree) throws IOException;

    default void visit(List<Tree> trees) throws IOException {
        for (Tree tree: trees) {
            tree.accept(this);
        }
    }
}
