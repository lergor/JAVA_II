package ru.ifmo.torrent.client.leech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.torrent.client.Client;
import ru.ifmo.torrent.client.storage.LocalFilesManager;
import ru.ifmo.torrent.client.storage.PartsManager;
import ru.ifmo.torrent.tracker.state.SeedInfo;
import ru.ifmo.torrent.util.TorrentException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Downloader implements Runnable, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    private final int DOWNLOADS_LIMIT = 5;

    private final ExecutorService pool = Executors.newFixedThreadPool(DOWNLOADS_LIMIT);
    private final LocalFilesManager filesManager;
    private final Client client;
    private final Set<FilePart> downloadingParts;
    private boolean shouldExit = false;

    public Downloader(LocalFilesManager filesManager, Client client) {
        this.filesManager = filesManager;
        downloadingParts = new HashSet<>();
        this.client = client;
    }

    @Override
    public void close() {
        shouldExit = true;
        pool.shutdown();
    }

    public void updateDownloads() {
        Set<FilePart> partsToDownload = filesManager.getFiles().stream().flatMap(
            f -> f.getMissingParts().stream()
                .map(p -> new FilePart(f.getFileId(), p))
                .filter(p -> !downloadingParts.contains(p)
                )
        ).collect(Collectors.toSet());

        Set<Integer> fileIds = partsToDownload.stream().map(FilePart::getFileId).collect(Collectors.toSet());
        if(!fileIds.isEmpty()) {
            logger.debug("update downloads for files " + fileIds);
        }

        Map<Integer, List<SeedInfo>> sourcesForFile = getSourcesForFiles(fileIds);
        partsToDownload.stream().filter(p -> !sourcesForFile.get(p.getFileId()).isEmpty())
            .limit(DOWNLOADS_LIMIT - downloadingParts.size())
            .forEach(p -> downloadPart(p, sourcesForFile.get(p.getFileId())));
    }

    private Map<Integer, List<SeedInfo>> getSourcesForFiles(Set<Integer> fileIds) {
        Map<Integer, List<SeedInfo>> sources = new HashMap<>();
        for (Integer fileId : fileIds) {
            sources.put(fileId, client.getFileSources(fileId));
        }
        return sources;
    }

    private void downloadPart(FilePart part, List<SeedInfo> sources) {
        if (!sources.isEmpty()) {
            logger.debug("part to download: " + part.fileId +" " + part.num);
            pool.submit(new DownloadTask(part, sources));
        }
    }

    @Override
    public void run() {
        while (!shouldExit) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
            updateDownloads();
        }
    }

    private class DownloadTask implements Runnable {

        private final FilePart part;
        private final List<SeedInfo> sources;
        private PartsManager partsManager = filesManager.getPartsManager();

        DownloadTask(FilePart part, List<SeedInfo> sources) {
            this.part = part;
            this.sources = sources;
            downloadingParts.add(part);
        }

        @Override
        public void run() {
            Optional<SeedInfo> maybeSource = getSource();
            if (!maybeSource.isPresent()) return;

            SeedInfo source = maybeSource.get();
            try (Leecher leecher = new Leecher(source.port(), source.inetAddress())) {
                byte[] content = leecher.getPartContent(part.getFileId(), part.getPartNum());

                try (OutputStream out = partsManager.getForWriting(part.getFileId(), part.getPartNum())) {
                    out.write(content);
                    out.flush();
                }

            } catch (TorrentException | IOException e) {
                e.printStackTrace();
                return;
            }

            try {
                filesManager.addReadyPartOfFile(part.getFileId(), part.getPartNum());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            downloadingParts.remove(part);
        }

        private Optional<SeedInfo> getSource() {
            for (SeedInfo s : sources) {
                try (Leecher leecher = new Leecher(s.port(), s.inetAddress())) {
                    List<Integer> availableParts = leecher.getAvailableParts(part.getFileId());
                    if (availableParts.contains(part.getPartNum())) {
                        return Optional.of(s);
                    }
                } catch (TorrentException | IOException e) {
                    e.printStackTrace();
                }
            }
            return Optional.empty();
        }
    }

    private class FilePart {
        private final int fileId;
        private final int num;

        public FilePart(int fileId, int partNum) {
            this.fileId = fileId;
            this.num = partNum;
        }

        public int getFileId() {
            return fileId;
        }

        public int getPartNum() {
            return num;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FilePart filePart = (FilePart) o;
            return fileId == filePart.fileId && num == filePart.num;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileId, num);
        }
    }
}