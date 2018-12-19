package ru.ifmo.torrent.tracker.state;

import ru.ifmo.torrent.util.StoredState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TrackerState implements StoredState {

    private final ConcurrentHashMap<Integer, FileInfo> IDToInfo = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, HashSet<SeedInfo>> IDToSources = new ConcurrentHashMap<>();

    public TrackerState() {}

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
        IDToSources.get(fileID).add(source);
    }

    public synchronized List<SeedInfo> getSources(int fileId) {
        return new ArrayList<>(IDToSources.get(fileId));
    }

    @Override
    public synchronized void storeToFile(Path metaFile) throws IOException {
        if (Files.notExists(metaFile)) {
            Files.createFile(metaFile);
        }
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(metaFile))) {
            out.writeInt(IDToInfo.size());
            for (FileInfo info : IDToInfo.values()) {
                info.write(out);
            }
        }
    }

    @Override
    public void restoreFromFile(Path metaFile) throws IOException {
        if (Files.notExists(metaFile)) {
            Files.createFile(metaFile);
            return;
        }
        if (Files.size(metaFile) == 0) return;
        try (DataInputStream in = new DataInputStream(Files.newInputStream(metaFile))) {
            int filesNumber = in.readInt();
            for (int i = 0; i < filesNumber; ++i) {
                FileInfo fileInfo = FileInfo.readFileInfo(in);
                IDToInfo.put(fileInfo.fileID(), fileInfo);
            }
        }
    }
}
