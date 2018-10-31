package ru.ifmo.git.tree.visitors;

import ru.ifmo.git.tree.TreeDirectory;
import ru.ifmo.git.tree.TreeFile;
import ru.ifmo.git.tree.TreeVisitor;

import java.io.IOException;
import java.nio.file.Files;


public class DeleteVisitor implements TreeVisitor {

    @Override
    public void visit(TreeFile tree) throws IOException {
        Files.deleteIfExists(tree.fullPath());
    }

    @Override
    public void visit(TreeDirectory tree) throws IOException {
        visit(tree.children());
        if (Files.list(tree.fullPath()).count() == 0) {
            Files.deleteIfExists(tree.fullPath());
        }
    }
}
