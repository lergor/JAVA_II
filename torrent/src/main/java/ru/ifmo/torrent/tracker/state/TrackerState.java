package ru.ifmo.torrent.tracker.state;

import ru.ifmo.torrent.util.StoredState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrackerState implements StoredState {

    private final ConcurrentHashMap<Integer, FileInfo> IDToInfo = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Set<SeedInfo>> IDToSources = new ConcurrentHashMap<>();
    private final Path metaFile;

    public TrackerState(Path metaFile) throws IOException {
        this.metaFile = metaFile;
        restoreFromFile();
    }

    public synchronized int addFile(String name, long size) {
        int ID = generateID();
        IDToInfo.put(ID, new FileInfo(ID, name, size));
        IDToSources.put(ID, new HashSet<>());
        return ID;
    }

    public synchronized List<FileInfo> getAvailableFiles() {
        return new ArrayList<>(IDToInfo.values());
    }

    private synchronized int generateID() {
        return IDToInfo.size();
    }

    public synchronized void addNewSeedIfAbsent(int fileID, SeedInfo source) {
        IDToSources.computeIfAbsent(fileID, id -> new HashSet<>()).add(source);
    }

    public synchronized List<SeedInfo> getSources(int fileId) {
        return new ArrayList<>(IDToSources.getOrDefault(fileId, Collections.emptySet()));
    }

    @Override
    public synchronized void storeToFile() throws IOException {
        if (Files.notExists(metaFile)) {
            Files.createFile(metaFile);
        }
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(metaFile))) {
            out.writeInt(IDToInfo.size());
            for (FileInfo info : IDToInfo.values()) {
                info.write(out);
            }
            out.flush();
        }
    }

    @Override
    public void restoreFromFile() throws IOException {
        if (Files.notExists(metaFile)) {
            Files.createFile(metaFile);
            return;
        }
        if (Files.size(metaFile) == 0) return;
        try (DataInputStream in = new DataInputStream(Files.newInputStream(metaFile))) {
            int filesNumber = in.readInt();
            for (int i = 0; i < filesNumber; ++i) {
                FileInfo fileInfo = FileInfo.readFrom(in);
                IDToInfo.put(fileInfo.getId(), fileInfo);
            }
        }
    }
}
