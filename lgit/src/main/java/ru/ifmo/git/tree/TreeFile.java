package ru.ifmo.git.tree;

import org.apache.commons.io.FileUtils;
import ru.ifmo.git.util.BlobType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class TreeFile extends Tree {

    private StringBuilder content = new StringBuilder();

    TreeFile() {}

    TreeFile(Path file, Path root) throws IOException {
        super(file, root);
        setType(BlobType.FILE);
        if(Files.exists(file)) {
            content.append(FileUtils.readFileToString(file.toFile()));
            setHash(TreeEncoder.getFileHash(fullPath()));
        }
    }

    @Override
    public void accept(TreeVisitor visitor) throws IOException {
        visitor.visit(this);
    }

    @Override
    public Tree find(String file) {
        if(path.equals(file)) {
            return this;
        }
        return null;
    }

    public String content() {
        return content.toString();
    }

    public List<String> contentAsLines() {
        return Arrays.asList(content().split(System.lineSeparator()));
    }

    public void setContent(String content) {
        this.content = new StringBuilder(content);
    }
}
