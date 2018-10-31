package ru.ifmo.git.tree.visitors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ru.ifmo.git.entities.GitFileManager;
import ru.ifmo.git.tree.Tree;
import ru.ifmo.git.tree.TreeDirectory;
import ru.ifmo.git.tree.TreeFile;
import ru.ifmo.git.tree.TreeVisitor;
import ru.ifmo.git.structs.Usages;

public class CleanerVisitor implements TreeVisitor {

    private Usages usages;
    private Path storage;

    public CleanerVisitor(Usages usages, Path storage) {
        this.usages = usages;
        this.storage = storage;
    }

    private void decrementAndDelete(Tree tree) throws IOException {
        if (!usages.decrement(tree.hash())) {
            Path file = storage.resolve(GitFileManager.pathInStorage(storage, tree.hash()));
            String[] files = file.getParent().toFile().list();
            if (files == null || files.length == 0) {
                Files.deleteIfExists(file);
            }
            Files.deleteIfExists(file);
        }
    }

    @Override
    public void visit(TreeFile tree) throws IOException {
        decrementAndDelete(tree);
    }

    @Override
    public void visit(TreeDirectory tree) throws IOException {
        visit(tree.children());
        decrementAndDelete(tree);
    }

    public Usages usages() {
        return usages;
    }
}
