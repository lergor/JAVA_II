package ru.ifmo.git.entities;

import ru.ifmo.git.tree.Tree;
import ru.ifmo.git.tree.TreeEncoder;
import ru.ifmo.git.tree.visitors.SaverVisitor;
import ru.ifmo.git.util.BlobType;
import ru.ifmo.git.util.GitException;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GitFileManager {

    private static final String METADIR = ".l_git";
    private final Path storage;
    private static final int dirNameLen = 2;

    GitFileManager(GitStructure gitStructure) {
        storage = gitStructure.storage();
    }

    public Path getDir(String blob) {
        return storage.resolve(blob.substring(0, dirNameLen));
    }

    public Path correctPath(String blob) {
        return getDir(blob).resolve(blob.substring(dirNameLen));
    }

    public Optional<Path> findFileInStorage(String hash) throws IOException {
        if(!hash.isEmpty()) {
            List<Path> blobs;
            blobs = Files.list(getDir(hash)).collect(Collectors.toList());
            for (Path blob : blobs) {
                if (blob.toFile().getName().startsWith(hash.substring(dirNameLen))) {
                    return Optional.of(blob);
                }
            }
        }
        return Optional.empty();
    }

    static public void clearDirectory(Path directory) throws IOException {
        List<Path> files = Files.list(directory)
                .filter(f -> !f.toString().contains(METADIR))
                .collect(Collectors.toList());
        removeAll(files);
    }

    static public void removeAll(List<Path> files) throws IOException {
        for (Path file : files) {
            if (Files.isDirectory(file)) {
                clearDirectory(file);
            }
            Files.deleteIfExists(file);
        }
    }

    static public boolean checkFilesExist(List<Path> files) {
        for (Path file : files) {
            if (!Files.exists(file)) {
                System.out.println("fatal: pathspec " + file.getFileName() + " did not match any files");
                return false;
            }
        }
        return true;
    }

    public static Path pathInStorage(Path storage, String hash) {
        return storage
                .resolve(hash.substring(0, dirNameLen))
                .resolve(hash.substring(dirNameLen));
    }

    public boolean restoreCommit(String revision, Path destination) throws IOException, GitException {
        Optional<Path> commit = findFileInStorage(revision);
        if (commit.isPresent()) {
            TreeEncoder encoder = new TreeEncoder(storage);
            Tree tree = encoder.decode(commit.get());
            if (!tree.type().equals(BlobType.COMMIT)) {
                return false;
            }
            tree.setRoot(destination);
            tree.accept(new SaverVisitor());
            return true;
        }
        return true;
    }
}
