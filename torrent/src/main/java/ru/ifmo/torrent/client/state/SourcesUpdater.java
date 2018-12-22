package ru.ifmo.torrent.client.state;

import ru.ifmo.torrent.client.Client;
import ru.ifmo.torrent.client.ClientConfig;
import ru.ifmo.torrent.messages.client_tracker.requests.UpdateRequest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SourcesUpdater implements AutoCloseable {

    private final ScheduledExecutorService pool;
    private final LocalFilesManager filesManager;
    private final Client client;
    private final short clientPort;

    public SourcesUpdater(Client client, LocalFilesManager filesManager, short clientPort) {
        pool = Executors.newScheduledThreadPool(1);
        pool.scheduleAtFixedRate(this::updateSources, 0, ClientConfig.UPDATE_RATE, TimeUnit.MILLISECONDS);
        this.filesManager = filesManager;
        this.client = client;
        this.clientPort = clientPort;
    }

    public void updateSources() {
        List<Integer> fileIds = filesManager.getFiles().stream()
            .filter( f -> !f.getReadyParts().isEmpty())
            .map(LocalFileReference::getFileId)
            .collect(Collectors.toList());

        try {
            client.sendRequest(new UpdateRequest(clientPort, fileIds));
        } catch (IOException e) {
        }
    }

    @Override
    public void close() {
        pool.shutdown();
    }
}