package ru.ifmo.torrent.client.storage;

import ru.ifmo.torrent.client.ClientConfig;
import ru.ifmo.torrent.util.StoredState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocalFilesManager implements StoredState {

    private ConcurrentHashMap<Integer, LocalFileReference> localFiles = new ConcurrentHashMap<>();
    private PartsManager partsManager;
    private final Path metaFile;

    public LocalFilesManager(Path metaFile) throws IOException {
        this.metaFile = metaFile.resolve("manager_file");
        if (Files.notExists(this.metaFile)) {
            this.metaFile.getParent().toFile().mkdirs();
            Files.createFile(this.metaFile);
            return;
        }
        partsManager = new PartsManager(ClientConfig.getLocalFilesStorage());
    }

    public void addLocalFile(String name, int fileId, long size) {
        int partsNum = getPartsNumber(size);
        if (localFiles.putIfAbsent(fileId, LocalFileReference.createFull(name, fileId, partsNum)) != null) {
            throw new IllegalArgumentException("file with id " + fileId + " already added");
        }
    }

    public void addNotDownloadedFile(String name, int fileId, long size) {
        Path file = metaFile.resolve(name);
        LocalFileReference reference = LocalFileReference.createEmpty(name, fileId, getPartsNumber(size));
        localFiles.put(fileId, reference);
    }

    public void addReadyPartOfFile(int fileId, int part) throws IOException {
        getOrThrow(fileId).addReadyPart(part);
        if (getOrThrow(fileId).getMissingParts().isEmpty()) {
            String fileName = localFiles.get(fileId).getName();
            partsManager.mergeSplitted(fileId, ClientConfig.TORRENT_DIR.resolve(fileName));
        }
    }

    private LocalFileReference getOrThrow(int fileId) {
        return Objects.requireNonNull(
            localFiles.get(fileId),
            "No file with id " + fileId
        );
    }

    public LocalFileReference getFileReference(int fileId) {
        return getOrThrow(fileId);
    }

    public List<LocalFileReference> getFiles() {
        return new ArrayList<>(localFiles.values());
    }

    private int getPartsNumber(long size) {
        return (int) Math.ceil(size / (double) ClientConfig.FILE_PART_SIZE);
    }

    @Override
    public void storeToFile() throws IOException {
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(metaFile))) {
            out.writeInt(localFiles.size());
            for (LocalFileReference file : localFiles.values()) {
                file.write(out);
            }
            out.flush();
        }
    }

    @Override
    public void restoreFromFile() throws IOException {
        if (Files.size(metaFile) == 0) return;
        localFiles = new ConcurrentHashMap<>();
        try (DataInputStream in = new DataInputStream(Files.newInputStream(metaFile))) {
            int numOfLocalFiles = in.readInt();
            for (int i = 0; i < numOfLocalFiles; i++) {
                LocalFileReference file = LocalFileReference.readFrom(in);
                localFiles.put(file.getFileId(), file);
            }
        }
    }

    public PartsManager getPartsManager() {
        return partsManager;
    }
}
