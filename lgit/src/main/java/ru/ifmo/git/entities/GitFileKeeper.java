package ru.ifmo.git.entities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import ru.ifmo.git.util.BlobType;
import ru.ifmo.git.util.FileReference;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GitFileKeeper {

    private final Path storage;
    private static final int dirNameLen = 2;

    GitFileKeeper(GitTree gitTree) {
        storage = gitTree.storage();
    }

    private Path getDir(String blob) {
        return storage.resolve(blob.substring(0, dirNameLen));
    }

    public Path correctPath(String blob) {
        return getDir(blob).resolve(blob.substring(dirNameLen));
    }

    private Path filePath(String blob) {
        Path directory = getDir(blob);
        if (Files.notExists(directory)) {
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

    public Optional<Path> findFileInStorage(String hash) throws IOException {
        List<Path> blobs;
        blobs = Files.list(getDir(hash)).collect(Collectors.toList());
        for (Path blob : blobs) {
            if (blob.toFile().getName().startsWith(hash.substring(dirNameLen))) {
                return Optional.of(blob);
            }
        }
        return Optional.empty();
    }

    public void restoreCommit(List<FileReference> references, Path destination) throws IOException {
        for (FileReference reference : references) {
            Path file = destination.resolve(reference.name);
            if (reference.type.equals(BlobType.FILE)) {
                if(Files.notExists(file)) {
                    Path dirs = file.getParent();
                    if(!destination.equals(dirs)) {
                        boolean ignored = dirs.toFile().mkdirs();
                    }
                    Files.createFile(file);
                }
                Files.copy(reference.content, file, StandardCopyOption.REPLACE_EXISTING);
            } else {
                boolean ignored = file.toFile().mkdirs();
            }
        }
    }

    static public void copyAll(List<Path> files, Path source, Path targetDir) throws IOException {
        File destination = targetDir.toFile();
        if (destination.exists() || (!destination.exists() && destination.mkdirs())) {
            for (Path file : files) {
                Path newFile = targetDir.resolve(source.relativize(file));
                if (Files.isRegularFile(file)) {
                    FileUtils.writeLines(newFile.toFile(), Files.readAllLines(file), System.lineSeparator());
                } else if (Files.isDirectory(file)) {
                    FileUtils.copyDirectoryToDirectory(file.toFile(), newFile.getParent().toFile());
                }
            }
        }
    }

    static public void clearDirectory(Path directory) throws IOException {
        List<Path> files = Files.list(directory).collect(Collectors.toList());
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

}
