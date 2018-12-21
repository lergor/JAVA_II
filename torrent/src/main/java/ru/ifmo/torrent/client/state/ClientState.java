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


public class ClientState implements StoredState {

    private ConcurrentHashMap<Integer, LocalFileState> localFiles = new ConcurrentHashMap<>();

    public ClientState() {
    }

    public void addLocalFile(int fileId, long size) throws IOException {
        int partsNum = getPartsNumber(size);
        if (localFiles.putIfAbsent(fileId, LocalFileState.createFull(fileId, partsNum)) != null) {
            throw new IllegalArgumentException("file with id " + fileId + " already added");
        }
    }

    public void addNotDownloadedFile(int fileId, long size) throws IOException {
        int partsNum = getPartsNumber(size);
        if (localFiles.putIfAbsent(fileId, LocalFileState.createEmpty(fileId, partsNum)) != null) {
            throw new IllegalArgumentException("file with id " + fileId + " already added");
        }
    }

    public List<Integer> getReadyParts(int fileId) {
        return getOrThrow(fileId).getReadyParts();
    }

    public void addReadyPartOfFile(int fileId, int part) {
        getOrThrow(fileId).addReadyPart(part);
    }

    private LocalFileState getOrThrow(int fileId) {
        return Objects.requireNonNull(
            localFiles.get(fileId),
            "No file with id " + fileId
        );
    }

    public LocalFileState getFileState(int fileId) {
        return getOrThrow(fileId);
    }

    public List<LocalFileState> getFiles() {
        return new ArrayList<>(localFiles.values());
    }

    private int getPartsNumber(long size) {
        return (int) Math.ceil(size / (double) ClientConfig.FILE_PART_SIZE);
    }

    @Override
    public void storeToFile(Path metaFile) throws IOException {
        if (Files.notExists(metaFile)) {
            Files.createFile(metaFile);
        }
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(metaFile))) {
            out.writeInt(localFiles.size());
            for (LocalFileState file : localFiles.values()) {
                file.write(out);
            }
            out.flush();
        }
    }

    @Override
    public void restoreFromFile(Path metaFile) throws IOException {
        if (Files.notExists(metaFile)) {
            Files.createFile(metaFile);
            return;
        }
        try (DataInputStream in = new DataInputStream(Files.newInputStream(metaFile))) {
            int numOfLOcalFiles = in.readInt();
            localFiles = new ConcurrentHashMap<>();
            for (int i = 0; i < numOfLOcalFiles; i++) {
                LocalFileState file = LocalFileState.readFrom(in);
                localFiles.put(file.getFileId(), file);
            }
        }
    }
}
