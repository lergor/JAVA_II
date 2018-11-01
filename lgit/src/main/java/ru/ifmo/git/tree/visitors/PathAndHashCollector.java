package ru.ifmo.git.tree.visitors;

import ru.ifmo.git.tree.TreeDirectory;
import ru.ifmo.git.tree.TreeFile;
import ru.ifmo.git.tree.TreeVisitor;
import ru.ifmo.git.util.GitException;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

public class PathAndHashCollector implements TreeVisitor {

    private Map<String, String> paths = new HashMap<>();

    @Override
    public void visit(TreeFile tree) {
        paths.put(tree.path(), tree.hash());
    }

    @Override
    public void visit(TreeDirectory tree) throws IOException, GitException {
        paths.put(tree.path(), tree.hash());
        visit(tree.children());
    }

    public Map<String, String> getPathsToHashes() {
        return paths;
    }

}
