package ru.ifmo.torrent.client;

import ru.ifmo.torrent.client.state.LocalFilesManager;
import ru.ifmo.torrent.client.state.PartsManager;
import ru.ifmo.torrent.client.state.LocalFileReference;
import ru.ifmo.torrent.client.state.SourcesUpdater;
import ru.ifmo.torrent.messages.client_tracker.requests.*;
import ru.ifmo.torrent.messages.client_tracker.response.*;
import ru.ifmo.torrent.messages.Request;
import ru.ifmo.torrent.messages.Response;
import ru.ifmo.torrent.tracker.state.FileInfo;
import ru.ifmo.torrent.tracker.state.SeedInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Client implements AutoCloseable {
    private static final int TRACKER_PORT = 8081;

    private LocalFilesManager localFilesManager;
    private PartsManager partsManager;

    private Socket clientSocket;
    private InetAddress inetAddress;
    private short port;
//    private Seed seed;
    private final SourcesUpdater sourcesUpdater;

    public Client(InetAddress inetAddress, short port) throws IOException {
//        this.inetAddress = InetAddress.getByName("192.168.211.41");
        this.inetAddress = inetAddress;
        this.port = port;

        localFilesManager = new LocalFilesManager(ClientConfig.getLocalFilesFile());
        partsManager = new PartsManager(ClientConfig.getLocalFilesStorage());
        sourcesUpdater = new SourcesUpdater(this, localFilesManager, port);
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
    public void close() throws IOException {
        localFilesManager.storeToFile();
//        seed.close();
        sourcesUpdater.close();
//        clientSocket.close();
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

    public boolean update() throws IOException {
        List<Integer> fileIds = localFilesManager.getFiles().stream()
            .filter( f -> f.getReadyParts().size() != 0)
            .map(LocalFileReference::getFileId).collect(Collectors.toList());
        UpdateRequest request = new UpdateRequest((short) clientSocket.getPort(), fileIds);
        UpdateResponse response = (UpdateResponse) sendRequest(request);
        return response.getResult();
    }

    public void downloadFile(int fileId) throws IOException {
        if(partsManager.fileIsPresent(fileId)) {
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