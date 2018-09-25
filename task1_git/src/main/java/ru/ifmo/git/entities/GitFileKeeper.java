package ru.ifmo.git.entities;

import org.apache.commons.io.FileUtils;
import ru.ifmo.git.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class GitFileKeeper {

    private Path storage;
    private GitTree gitTree;
    private static final int dirNameLen = 2;

    public GitFileKeeper(GitTree gitTree) {
        this.gitTree = gitTree;
        storage = gitTree.storage();
    }

    public static Path withDir(Path file) {
        String fileName = file.toFile().getName();
        return Paths.get(fileName.substring(0, dirNameLen),
                fileName.substring(dirNameLen)).toAbsolutePath();
    }

    public Path getDir(String blob) {
        return storage.resolve(blob.substring(0, dirNameLen));
    }

    public Path correctPath(String blob) {
        return getDir(blob).resolve(blob.substring(dirNameLen));
    }

    private Path filePath(String blob) throws IOException {
        Path directory = getDir(blob);
        if(Files.notExists(directory)) {
            boolean ignored = directory.toFile().mkdirs();
        }
        return directory.resolve(blob.substring(dirNameLen));
    }

    public void saveCommit(List<FileReference> references) throws IOException {
        for (FileReference reference : references) {
            Path file = filePath(reference.name);
            Files.copy(reference.content, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void saveToIndex(List<FileReference> references) throws IOException {
        for (FileReference reference : references) {
            Path file = gitTree.index().resolve(reference.name);
            Files.copy(reference.content, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public Optional<Path> findFileInStorage(String hash) throws IOException {
        List<Path> blobs = Files.list(getDir(hash)).collect(Collectors.toList());
        for (Path blob: blobs) {
            if(blob.getFileName().startsWith(hash.substring(dirNameLen))) {
                return Optional.of(blob);
            }
        }
        return Optional.empty();
    }

    public void restoreCommit(List<FileReference> references, Path destination) throws IOException {
        for (FileReference reference : references) {
            Path file = destination.resolve(reference.name);
            if(reference.type.equals(BlobType.FILE)) {
                Files.copy(reference.content, file, StandardCopyOption.REPLACE_EXISTING);
            } else {
                boolean ignored = file.toFile().mkdirs();
            }
        }
    }

    public static void deleteFile(Path file) throws GitException {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new GitException(e.getMessage());
        }
    }

    static public void copyAll(List<Path> files, Path targetDir) throws IOException {
        File destination = targetDir.toFile();
        if (destination.exists() || (!destination.exists() && destination.mkdirs())) {
            for (Path f : files) {
                File file = f.toFile();
                if (file.isFile()) {
                    FileUtils.copyFileToDirectory(file, destination);
                } else if (file.isDirectory()) {
                    FileUtils.copyDirectoryToDirectory(file, destination);
                }
            }
        }
    }

    static public void clearDirectory(Path directory) throws GitException {
        try {
            List<Path> files = Files.list(directory).collect(Collectors.toList());
            removeAll(files);
        } catch (IOException e) {
            e.printStackTrace();
            throw new GitException("error while removing");
        }
    }

    static public void removeAll(List<Path> files) throws GitException {
        try{
            for (Path file : files) {
                if(Files.isRegularFile(file)) {
                    deleteFile(file);
                } else {
                    clearDirectory(file);
                    Files.deleteIfExists(file);
                }
            }
        } catch (IOException e) {
            throw new GitException(e.getMessage());
        }
    }
}
