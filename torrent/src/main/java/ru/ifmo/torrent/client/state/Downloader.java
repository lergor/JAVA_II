package ru.ifmo.torrent.client.state;

import org.apache.commons.io.IOUtils;
import ru.ifmo.torrent.client.Client;
import ru.ifmo.torrent.client.leech.Leech;
import ru.ifmo.torrent.tracker.state.SeedInfo;
import ru.ifmo.torrent.util.TorrentException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Downloader implements AutoCloseable {
    private final int DOWNLOADS_LIMIT = 5;

    private final ExecutorService pool = Executors.newFixedThreadPool(DOWNLOADS_LIMIT);
    private final LocalFilesManager filesManager;
    private final Client client;
    private final Set<FilePart> downloadingParts;

    public Downloader(LocalFilesManager filesManager, Client client) {
        this.filesManager = filesManager;
        downloadingParts = new HashSet<>();
        this.client = client;
    }

    @Override
    public void close() {
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
            pool.submit(new DownloadTask(part, sources));
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
            try (Leech leech = new Leech(source.port(), source.inetAddress())) {
                InputStream in = leech.getPartContent(part.getFileId(), part.getPartNum());

                try (OutputStream out = partsManager.getForWriting(part.getFileId(), part.getPartNum())) {
                    IOUtils.copy(in, out);
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
                try (Leech leech = new Leech(s.port(), s.inetAddress())) {
                    List<Integer> availableParts = leech.getAvailableParts(part.getFileId());
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