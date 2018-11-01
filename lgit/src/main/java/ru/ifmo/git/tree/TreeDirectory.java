package ru.ifmo.git.tree;

import ru.ifmo.git.util.BlobType;
import ru.ifmo.git.util.GitException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TreeDirectory extends Tree {

    private Map<Integer, List<Tree>> children = new HashMap<>();

    TreeDirectory() {
    }

    TreeDirectory(Path directory, Path root) throws IOException {
        super(directory, root);
        setType(BlobType.DIRECTORY);
        List<Path> files = Files.list(directory).collect(Collectors.toList());
        for (Path file : files) {
            if (!file.getFileName().toString().contains(METADIR)) {
                addChild(Tree.createTree(file, root));
            }
        }
        setHash(TreeEncoder.getDirectoryHash(path, children()));
    }

    private int getIndex(String path) {
        Path local = Paths.get(this.path).relativize(Paths.get(path));
        if (!path.isEmpty() && local.getNameCount() != 0) {
            char letter = local.getName(0).toString().charAt(0);
            if (letter >= 'a') {
                return (letter - 'a');
            }
            return letter - 'A';
        }
        return 27;
    }

    public void addChild(Tree child) {
        Integer i = getIndex(child.path);
        if (!children.containsKey(i)) {
            children.put(i, new ArrayList<>());
        }
        children.get(i).add(child);
    }

    public List<Tree> children() {
        List<Tree> list = new ArrayList<>();
        children.forEach((i, l) -> list.addAll(l));
        return list;
    }

    public void setChildren(List<Tree> children) {
        this.children = new HashMap<>();
        children.forEach(this::addChild);
    }

    @Override
    public void accept(TreeVisitor visitor) throws IOException, GitException {
        visitor.visit(this);
    }

    @Override
    public void setRoot(Path root) {
        this.root = root;
        children.forEach((i, l) -> l.forEach(c -> c.setRoot(root)));
    }

    @Override
    public Tree find(String file) {
        if (type().equals(BlobType.COMMIT)) {
            return children().get(0).find(file);
        }
        if (path.equals(file)) {
            return this;
        }
        Integer i = getIndex(file);
        if (children.containsKey(i)) {
            for (Tree c : children.get(i)) {
                Tree result = c.find(file);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return type().equals(BlobType.DIRECTORY) && path.isEmpty() && children.isEmpty();
    }
}
