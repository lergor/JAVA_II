package ru.ifmo.git.tree.visitors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import ru.ifmo.git.tree.*;
import ru.ifmo.git.util.BlobType;
import ru.ifmo.git.util.GitException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class SaverVisitor implements TreeVisitor {

    private boolean mergeFiles = false;
    private String mergeBranch = "";
    private boolean conflictsAcquired = false;
    private Set<String> conflictingFiles = new HashSet<>();

    public SaverVisitor() {
    }

    public SaverVisitor(boolean merge, String mergeBranch) {
        mergeFiles = merge;
        this.mergeBranch = mergeBranch;
    }

    private static void copyContent(TreeFile file, Path newFile) throws IOException {
        FileUtils.writeStringToFile(newFile.toFile(), file.content());
    }

    @Override
    public void visit(TreeFile tree) throws IOException, GitException {
        Path file = tree.fullPath();
        if (Files.notExists(file)) {
            Files.createFile(file);
        }
        String content = IOUtils.toString(Files.newInputStream(file));
        if (!tree.content().equals(content)) {
            conflictsAcquired = true;
            if (!mergeFiles) {
                copyContent(tree, file);
            } else {
                mergeContent(tree, content, mergeBranch);
                conflictingFiles.add(tree.path());
            }
        }

    }

    public static void mergeContent(TreeFile file, String currentContent, String mergeBranch) throws GitException {
        Path newFile = file.fullPath();
        String sep = System.lineSeparator();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newFile.toFile(), false))) {
            writer.write("<<<<<<< HEAD" + sep);
            writer.write(currentContent);
            writer.write(sep + "=======" + sep);
            writer.write(file.content());
            writer.write(sep + ">>>>>>> " + mergeBranch + sep);
        } catch (IOException e) {
            throw new GitException("error while writing to " + newFile.getFileName());
        }
    }

    @Override
    public void visit(TreeDirectory tree) throws IOException, GitException {
        if (tree.type().equals(BlobType.DIRECTORY) && !tree.path().isEmpty()) {
            Path path = tree.fullPath();
            if (Files.notExists(path)) {
                Files.createDirectory(path);
            }
        }
        visit(tree.children());
    }

    public boolean conflictsAcquired() {
        return conflictsAcquired;
    }

    public Set<String> conflictingFiles() {
        return conflictingFiles;
    }
}
