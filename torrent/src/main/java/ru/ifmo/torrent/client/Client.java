package ru.ifmo.torrent.client;

import ru.ifmo.torrent.client.peer.Peer;
import ru.ifmo.torrent.client.seed.Seed;
import ru.ifmo.torrent.client.state.LocalFilesManager;
import ru.ifmo.torrent.client.state.StorageManager;
import ru.ifmo.torrent.client.state.LocalFileReference;
import ru.ifmo.torrent.messages.client_tracker.requests.ListRequest;
import ru.ifmo.torrent.messages.client_tracker.requests.SourcesRequest;
import ru.ifmo.torrent.messages.client_tracker.requests.UploadRequest;
import ru.ifmo.torrent.messages.client_tracker.response.ListResponse;
import ru.ifmo.torrent.messages.client_tracker.response.SourcesResponse;
import ru.ifmo.torrent.messages.client_tracker.response.UploadResponse;
import ru.ifmo.torrent.network.Request;
import ru.ifmo.torrent.network.Response;
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
import java.util.List;

public class Client implements AutoCloseable {
    private static final int TRACKER_PORT = 8081;

    private LocalFilesManager state;
    private StorageManager storageManager;

    private Socket clientSocket;
    private Seed seed;
    private Peer peer;

//    private final TrackerClient trackerClient;
//    private final LocalFilesManager localFilesManager;
//    private final Downloader downloader;
//    private final SourcesUpdater updater;
//    private final PeerServer server;

    private final DataOutputStream out;
    private final DataInputStream in;

    private Path metaDir = ClientConfig.getMetaDir();

    public Client(InetAddress inetAddress, short port) throws IOException {
        clientSocket = new Socket(inetAddress, TRACKER_PORT);

        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());

        Path metadir = metaDir.resolve(String.valueOf(port));
        state = new LocalFilesManager(metadir);
        storageManager = new StorageManager(metadir.resolve("file_manager"));

        seed = new Seed();
        Thread seedThread = new Thread(() -> seed.start((short) 1199, state));
        seedThread.start();
    }

    private Response sendRequest(Request request) throws IOException {
        request.write(out);
        out.flush();
        Response response = request.getEmptyResponse();
        response.read(in);
        return response;
    }

    @Override
    public void close() throws IOException, TorrentException {
        state.storeToFile();
        seed.stop();
        peer.close();
        clientSocket.close();
    }

    public List<FileInfo> getAvailableFiles() throws IOException {
        ListResponse response = (ListResponse) sendRequest(new ListRequest());
        return response.getFiles();
    }

    public int uploadFile(Path file) throws IOException {
        if(Files.notExists(file)) {
            throw new IllegalArgumentException("file '" + file + "' does not exists");
        }
        UploadResponse response = (UploadResponse) sendRequest(new UploadRequest(file));
        return response.getFileID();
    }

    public List<SeedInfo> getFileSources(int fileId) throws IOException {
        SourcesResponse response = (SourcesResponse) sendRequest(new SourcesRequest(fileId));
        return response.getClients();
    }

    public void downloadFile(int fileId) {
        if(storageManager.fileIsPresent(fileId)) {
            throw new IllegalArgumentException("file with id " + fileId + " already added as local file");
        }
        // TODO
    }

    List<LocalFileReference> getLocalFiles() {
        return state.getFiles();
    }


}