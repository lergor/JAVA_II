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

public class Client implements AutoCloseable {

    private static final int TRACKER_PORT = 8081;

    private LocalFilesManager localFilesManager;

    private InetAddress inetAddress;
    private Downloader downloader;
    private Seeder seeder;
    private final SourcesUpdater sourcesUpdater;

    public Client(InetAddress inetAddress, short port) throws IOException {
        this.inetAddress = inetAddress;

        localFilesManager = new LocalFilesManager(ClientConfig.getLocalFilesFile());
        localFilesManager.restoreFromFile();
        sourcesUpdater = new SourcesUpdater(this, localFilesManager, port);

        this.downloader = new Downloader(localFilesManager, this);
        Thread downloaderTread = new Thread(downloader);
        downloaderTread.start();

        this.seeder = new Seeder(port, localFilesManager);
        Thread seedTread = new Thread(seeder);
        seedTread.start();

    }

    public Response sendRequest(Request request) throws IOException {
        try(Socket clientSocket = new Socket(inetAddress, TRACKER_PORT)) {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            Response response;
            try {
                request.write(out);
                out.flush();
                response = request.getEmptyResponse();
                response.read(in);
            } catch (IOException e) {
                throw new IllegalStateException("cannot send request " + request.getClass().getSimpleName(), e);
            }
            return response;
        }

    }

    @Override
    public void close() throws IOException, TorrentException {
        localFilesManager.storeToFile();
        downloader.close();
        sourcesUpdater.close();
        seeder.close();
    }

    public List<FileInfo> getAvailableFiles() throws IOException {
        ListResponse response = (ListResponse) sendRequest(new ListRequest());
        return response.getFiles();
    }

    public int uploadFile(Path file) throws IOException {
        if(Files.notExists(file)) {
            throw new IllegalArgumentException("file '" + file + "' does not exists");
        }
        UploadRequest request = new UploadRequest(file);
        UploadResponse response = (UploadResponse) sendRequest(request);
        localFilesManager.getPartsManager().storeSplitted(response.getFileID(), file);
        localFilesManager.addLocalFile(file.getFileName().toString(), response.getFileID(), request.getFileSize());
        return response.getFileID();
    }

    public List<SeedInfo> getFileSources(int fileId) {
        SourcesResponse response = null;
        try {
            response = (SourcesResponse) sendRequest(new SourcesRequest(fileId));
        } catch (IOException e) {
            return new ArrayList<>();
        }
        return response.getClients();
    }

    public void downloadFile(int fileId) throws IOException {
        if(localFilesManager.getPartsManager().fileIsPresent(fileId)) {
            throw new IllegalArgumentException("file with id " + fileId + " already added as local file");
        }

        List<FileInfo> files = getAvailableFiles();
        FileInfo fileInfo = files.stream()
            .filter(f -> f.getId() == fileId)
            .findFirst().orElseThrow(() ->
            new IllegalArgumentException("File with id " + fileId + " does not exist!")
        );

        localFilesManager.addNotDownloadedFile(fileInfo.getName(), fileInfo.getId(), fileInfo.getSize());
    }

    List<LocalFileReference> getLocalFiles() {
        return localFilesManager.getFiles();
    }


}