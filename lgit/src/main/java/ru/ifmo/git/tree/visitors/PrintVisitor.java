package ru.ifmo.git.tree.visitors;

import ru.ifmo.git.tree.TreeDirectory;
import ru.ifmo.git.tree.TreeFile;
import ru.ifmo.git.tree.TreeVisitor;
import ru.ifmo.git.util.GitException;

import java.io.IOException;

public class PrintVisitor implements TreeVisitor {
    @Override
    public void visit(TreeFile tree) throws IOException, GitException {
        System.out.println(tree.type() + ": " + tree.path());
    }

    @Override
    public void visit(TreeDirectory tree) throws IOException, GitException {
        if (!tree.path().isEmpty()) {
            System.out.println(tree.type() + ": " + tree.path());
        }
        visit(tree.children());
    }
}
