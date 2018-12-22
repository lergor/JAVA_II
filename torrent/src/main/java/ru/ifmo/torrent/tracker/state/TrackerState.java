package ru.ifmo.torrent.tracker.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.torrent.client.ClientConfig;
import ru.ifmo.torrent.util.StoredState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TrackerState implements StoredState {

    private static final Logger logger = LoggerFactory.getLogger(TrackerState.class);
    private final ScheduledExecutorService pool;
    private final ConcurrentHashMap<Integer, FileInfo> IDToInfo = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Set<TimedSeedInfo>> IDToSources = new ConcurrentHashMap<>();

    private final Path metaFile;

    public TrackerState(Path metaFile) throws IOException {
        this.metaFile = metaFile;
        if (Files.notExists(metaFile)) {
            Files.createDirectories(metaFile.getParent());
            Files.createFile(metaFile);
        }
        pool = Executors.newScheduledThreadPool(1);
        pool.scheduleAtFixedRate(this::updateSeedList, 0, 180, TimeUnit.SECONDS);
        restoreFromFile();
    }

    public synchronized int addFile(String name, long size) {
        int ID = generateID();
        IDToInfo.put(ID, new FileInfo(ID, name, size));
        IDToSources.put(ID, Collections.synchronizedSet(new HashSet<>()));
        return ID;
    }

    public synchronized List<FileInfo> getAvailableFiles() {
        return new ArrayList<>(IDToInfo.values());
    }

    private synchronized int generateID() {
        return IDToInfo.size();
    }

    public synchronized void addNewSeedIfAbsent(int fileID, SeedInfo source) {
        long currentTime = Instant.now().toEpochMilli();
        IDToSources.computeIfAbsent(fileID, id ->
            Collections.synchronizedSet(new HashSet<>())).add(new TimedSeedInfo(source, currentTime));
    }

    public synchronized List<SeedInfo> getSources(int fileId) {
        return IDToSources.getOrDefault(fileId, new HashSet<>())
            .stream()
            .map(TimedSeedInfo::getSeedInfo)
            .collect(Collectors.toList());
    }

    @Override
    public synchronized void storeToFile() throws IOException {
        pool.shutdown();
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
        if (Files.size(metaFile) == 0) return;
        try (DataInputStream in = new DataInputStream(Files.newInputStream(metaFile))) {
            int filesNumber = in.readInt();
            for (int i = 0; i < filesNumber; ++i) {
                FileInfo fileInfo = FileInfo.readFrom(in);
                IDToInfo.put(fileInfo.getId(), fileInfo);
            }
        }
    }

    private void updateSeedList() {
        long currentTime = Instant.now().toEpochMilli();
        for (Map.Entry<Integer, Set<TimedSeedInfo>> fileToSources : IDToSources.entrySet()) {

            Set<TimedSeedInfo> values = fileToSources.getValue();
            synchronized (values) {
                int n = values.size();
                values.removeIf(s -> s.notAlive(currentTime));
                logger.debug("clearing sources, size before " + n + " after " + values.size());
            }
        }
    }

}
