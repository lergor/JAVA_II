package ru.ifmo.git.tree.visitors;

import org.apache.commons.io.FileUtils;
import ru.ifmo.git.tree.*;
import ru.ifmo.git.util.BlobType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SaverVisitor implements TreeVisitor {

    private void copyContent(TreeFile file, Path newFile) throws IOException {
        FileUtils.writeLines(
                newFile.toFile(),
                file.contentAsLines(),
                System.lineSeparator()
        );
    }

    @Override
    public void visit(TreeFile tree) throws IOException {
        Path file = tree.fullPath();
        if (Files.notExists(file)) {
            Files.createFile(file);
        }
        if (!tree.hash().equals(TreeEncoder.getFileHash(file))) {
            copyContent(tree, file);
        }
    }

    @Override
    public void visit(TreeDirectory tree) throws IOException {
        if (tree.type().equals(BlobType.DIRECTORY) && !tree.path().isEmpty()) {
            Path path = tree.fullPath();
            if (Files.notExists(path)) {
                Files.createDirectory(path);
            }
        }
        visit(tree.children());
    }
}
