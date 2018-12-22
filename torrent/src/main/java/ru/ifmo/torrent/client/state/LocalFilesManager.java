package ru.ifmo.torrent.client.state;

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
    private final Path metaFile;

    public LocalFilesManager(Path metadir) throws IOException {
        metaFile = metadir.resolve("client_state_file");
        if (Files.notExists(metaFile)) {
            metaFile.getParent().toFile().mkdirs();
            Files.createFile(metaFile);
            return;
        }
    }

    public void addLocalFile(String name, int fileId, long size) {
        int partsNum = getPartsNumber(size);
        if (localFiles.putIfAbsent(fileId, LocalFileReference.createFull(name, fileId, partsNum)) != null) {
            throw new IllegalArgumentException("file with id " + fileId + " already added");
        }
    }

    public void addNotDownloadedFile(String name, int fileId, long size) throws IOException {
        int partsNum = getPartsNumber(size);
        if (localFiles.putIfAbsent(fileId, LocalFileReference.createEmpty(name, fileId, partsNum)) != null) {
            throw new IllegalArgumentException("file with id " + fileId + " already added");
        }
    }

    public List<Integer> getReadyParts(int fileId) {
        return getOrThrow(fileId).getReadyParts();
    }

    public void addReadyPartOfFile(int fileId, int part) {
        getOrThrow(fileId).addReadyPart(part);
    }

    private LocalFileReference getOrThrow(int fileId) {
        return Objects.requireNonNull(
            localFiles.get(fileId),
            "No file with id " + fileId
        );
    }

    public LocalFileReference getFileState(int fileId) {
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
        try (DataInputStream in = new DataInputStream(Files.newInputStream(metaFile))) {
            int numOfLOcalFiles = in.readInt();
            localFiles = new ConcurrentHashMap<>();
            for (int i = 0; i < numOfLOcalFiles; i++) {
                LocalFileReference file = LocalFileReference.readFrom(in);
                localFiles.put(file.getFileId(), file);
            }
        }
    }
}
