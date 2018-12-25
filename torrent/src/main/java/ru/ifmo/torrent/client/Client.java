package ru.ifmo.torrent.client;

import ru.ifmo.torrent.client.leech.Downloader;
import ru.ifmo.torrent.client.seed.Seeder;
import ru.ifmo.torrent.client.storage.*;
import ru.ifmo.torrent.messages.client_tracker.requests.*;
import ru.ifmo.torrent.messages.client_tracker.response.*;
import ru.ifmo.torrent.messages.Request;
import ru.ifmo.torrent.messages.Response;
import ru.ifmo.torrent.tracker.state.FileInfo;
import ru.ifmo.torrent.tracker.state.SeedInfo;
import ru.ifmo.torrent.util.TorrentException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Client implements AutoCloseable {

    private InetAddress inetAddress;
    private LocalFilesManager localFilesManager;
    private Downloader downloader;
    private Seeder seeder;
    private final SourcesUpdater sourcesUpdater;

    public Client(InetAddress inetAddress, short port) throws IOException, TorrentException {
        this.inetAddress = inetAddress;
        localFilesManager = new LocalFilesManager(ClientConfig.getLocalFilesFile());
        sourcesUpdater = new SourcesUpdater(this, localFilesManager, port);

        this.downloader = new Downloader(localFilesManager, this);
        Thread downloaderTread = new Thread(downloader);
        downloaderTread.start();

        this.seeder = new Seeder(port, localFilesManager);
        Thread seedTread = new Thread(seeder);
        seedTread.start();

    }

    public Response sendRequest(Request request) throws IOException, TorrentException {
        try (Socket clientSocket = new Socket(inetAddress, ClientConfig.TRACKER_PORT)) {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            Response response;
            try {
                request.write(out);
                out.flush();
                response = request.getEmptyResponse();
                response.read(in);
            } catch (IOException e) {
                throw new TorrentException("cannot send request " + request.getClass().getSimpleName(), e);
            }
            return response;
        }

    }

    @Override
    public void close() throws TorrentException {
        localFilesManager.storeToFile();
        downloader.close();
        sourcesUpdater.close();
        seeder.close();
    }

    public List<FileInfo> getAvailableFiles() throws IOException, TorrentException {
        ListResponse response = (ListResponse) sendRequest(new ListRequest());
        return response.getFiles();
    }

    public int uploadFile(Path file) throws IOException, TorrentException {
        if (Files.notExists(file)) {
            throw new TorrentException("file '" + file + "' does not exists");
        }
        UploadRequest request = new UploadRequest(file);
        UploadResponse response = (UploadResponse) sendRequest(request);
        localFilesManager.addFileToStorageAsParts(response.getFileId(), file);
        localFilesManager.addLocalFile(file.getFileName().toString(), response.getFileId(), request.getFileSize());
        return response.getFileId();
    }

    public List<SeedInfo> getFileSources(int fileId) throws TorrentException {
        SourcesResponse response;
        try {
            response = (SourcesResponse) sendRequest(new SourcesRequest(fileId));
        } catch (IOException e) {
            return new ArrayList<>();
        } catch (TorrentException e) {
            throw new TorrentException("", e.getException());
        }
        return response.getClients();
    }

    public boolean downloadFile(int fileId) throws IOException, TorrentException {
        if (localFilesManager.getPartsManager().fileIsPresent(fileId)) {
            System.err.println("file with id " + fileId + " already added as local file");
            return false;
        }

        Optional<FileInfo> fileInfo = getAvailableFiles().stream()
            .filter(f -> f.getId() == fileId)
            .findFirst();

        if(!fileInfo.isPresent()) {
            System.err.println("File with id " + fileId + " does not exist!");
            return false;
        }
        FileInfo file = fileInfo.get();
        localFilesManager.addNotDownloadedFile(file.getName(), file.getId(), file.getSize());
        return true;
    }

    List<LocalFileReference> getLocalFiles() {
        return localFilesManager.getFiles();
    }

}
