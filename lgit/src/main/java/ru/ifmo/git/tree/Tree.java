package ru.ifmo.git.tree;

import ru.ifmo.git.util.BlobType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public abstract class Tree {

    protected static final String METADIR = ".l_git";
    protected Path root;
    protected String path;
    private BlobType type;
    private String hash = "";

    public static Tree createTree(Path file, Path root) throws IOException {
        return Files.isDirectory(file) ?
                new TreeDirectory(file, root) : new TreeFile(file, root);
    }

    public static Tree createTree(Path directory) throws IOException {
        return createTree(directory, directory);
    }

    public static List<Tree> createTrees(List<Path> files, Path root) throws IOException {
        List<Tree> list = new ArrayList<>();
        for (Path f: files) {
            list.add(createTree(f, root));
        }
        return list;
    }

    public static List<Tree> createDirectoryTrees(Path directory) throws IOException {
        return Tree.createTrees(
                Files   .list(directory)
                        .filter(f -> !f.toAbsolutePath().toString().contains(METADIR))
                        .collect(Collectors.toList()),
                directory);
    }

    Tree() {}

    Tree(Path file, Path root) {
        this.root = root;
        path = root.relativize(file).toString();
    }

    public abstract void accept(TreeVisitor visitor) throws IOException;

    public abstract Tree find(String path);

    public BlobType type() {
        return type;
    }

    public String path() {
        return path;
    }

    public String hash() {
        return hash;
    }

    public Path root() {
        return root;
    }

    public Path fullPath() {
        if (root != null) {
            return root.resolve(path);
        }
        return Paths.get(path);
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setType(BlobType type) {
        this.type = type;
    }

    public void setRoot(Path root) {
        this.root = root;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String info() {
        return type.asString() + hash + "\t" + path + System.lineSeparator();
    }

}
