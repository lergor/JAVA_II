package ru.ifmo.git.structs;

import ru.ifmo.git.tree.Tree;
import ru.ifmo.git.tree.visitors.PathAndHashCollector;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Status {

    public Set<String> newFiles = new HashSet<>();
    public Set<String> deletedFiles = new HashSet<>();
    public Set<String> modifiedFiles = new HashSet<>();

    private static final String sep = System.lineSeparator();

    public Status() {}

    public Status(Tree from, Tree to) throws IOException {
        buildStatus(from, to);
    }

    public void buildStatus(Tree from, Tree to) throws IOException {
        Map<String, String> toFiles = collectPaths(to);
        Map<String, String> fromFiles = collectPaths(from);

        Set<String> files = new HashSet<>(toFiles.keySet());
        for (String path : files) {
            String fromHash = fromFiles.remove(path);
            String toHash = toFiles.remove(path);
            if(!path.isEmpty()) {
                if (fromHash != null && toHash != null && !toHash.equals(fromHash)) {
                    modifiedFiles.add(path);
                } else if (fromHash == null && toHash != null) {
                    deletedFiles.add(path);
                } else if (fromHash != null && toHash == null) {
                    newFiles.add(path);
                }
            }
        }
        newFiles.addAll(fromFiles.keySet());
    }

    private static Map<String, String> collectPaths(Tree tree) throws IOException {
        PathAndHashCollector visitor = new PathAndHashCollector();
        tree.accept(visitor);
        return visitor.getPathsToHashes();
    }

    public Set<String> getNewFiles() {
        return newFiles;
    }

    public Set<String> getModifiedFiles() {
        return modifiedFiles;
    }

    public Set<String> getDeletedFiles() {
        return deletedFiles;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        writeInfo("new file: ", newFiles, builder);
        writeInfo("modified: ", modifiedFiles, builder);
        writeInfo("deleted: ", deletedFiles, builder);
        return builder.toString();
    }

    private static void writeInfo(String prefix, Set<String> files, StringBuilder builder) {
        files.forEach(f -> builder.append(prefix).append(f).append(sep));
        if(!files.isEmpty()) {
            builder.append(sep);
        }
    }

    public boolean isEmpty() {
        return newFiles.isEmpty() && modifiedFiles.isEmpty() && deletedFiles.isEmpty();
    }


    public String newFilesToString() {
        return filesToString(newFiles, "new file: ");
    }

    public String deletedFilesToString() {
        return filesToString(deletedFiles, "deleted: ");
    }

    public String modifiedFilesToString() {
        return filesToString(modifiedFiles, "modified: ");
    }

    private String filesToString(Set<String> files, String prefix) {
        StringBuilder builder = new StringBuilder();
        files.forEach(f -> builder.append(prefix).append(f).append(sep));
        if(!files.isEmpty()) {
            builder.append(sep);
        }
        return builder.toString();
    }
}
