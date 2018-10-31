package ru.ifmo.git.tree;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import ru.ifmo.git.util.BlobType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class TreeFile extends Tree {

    private InputStream content = IOUtils.toInputStream("");

    TreeFile() {}

    TreeFile(Path file, Path root) throws IOException {
        super(file, root);
        setType(BlobType.FILE);
        if(Files.exists(file)) {
            content = IOUtils.toInputStream(FileUtils.readFileToString(file.toFile()));
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

    public InputStream content() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }
}
