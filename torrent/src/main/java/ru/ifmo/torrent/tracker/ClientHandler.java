package ru.ifmo.torrent.tracker;

import ru.ifmo.torrent.messages.client_tracker.Marker;
import ru.ifmo.torrent.messages.client_tracker.requests.*;
import ru.ifmo.torrent.messages.client_tracker.response.*;
import ru.ifmo.torrent.network.Response;
import ru.ifmo.torrent.tracker.state.FileInfo;
import ru.ifmo.torrent.tracker.state.SeedInfo;
import ru.ifmo.torrent.tracker.state.TrackerState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private final Socket client;
    private final TrackerState trackerState;

    ClientHandler(Socket client, TrackerState trackerState) {
        this.client = client;
        this.trackerState = trackerState;
    }

    @Override
    public void run() {
        try (Socket clientSocket = client) {
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());

            Response response = null;
            int marker;
            while ((marker = in.read()) != -1) {
                switch (marker) {
                    case Marker.LIST:
                        response = new ListResponse(trackerState.getAvailableFiles());
                        break;
                    case Marker.SOURCES: {
                        SourcesRequest request = SourcesRequest.readFromDataInputStream(in);
                        int fileId = request.getFileId();
                        response = new SourcesResponse(fileId, trackerState.getSources(fileId));
                        break;
                    }
                    case Marker.UPDATE: {
                        UpdateRequest request = UpdateRequest.readFromDataInputStream(in);
                        SeedInfo newSeed = new SeedInfo(request.getClientPort(), clientSocket.getInetAddress());
                        boolean success = update(request.getFileIds(), newSeed);
                        response =  new UpdateResponse(success);
                        break;
                    }
                    case Marker.UPLOAD: {
                        UploadRequest request = UploadRequest.readFromDataInputStream(in);
                        int fileId = trackerState.addFile(request.getFileName(), request.getFileSize());
                        response = new UploadResponse(fileId);
                        break;
                    }
                    default:
                        break;
                }
                if(response != null) {
                    response.write(out);
                    out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private boolean update(List<Integer> fileIds, SeedInfo newSeed) {
        Set<Integer> allFiles = trackerState.getAvailableFiles().stream()
            .map(FileInfo::fileId)
            .collect(Collectors.toSet());
        if (!allFiles.containsAll(fileIds)) {
            return false;
        }
        for (int ID : fileIds) {
            trackerState.addNewSeedIfAbsent(ID, newSeed);
        }
        return true;
    }
}
