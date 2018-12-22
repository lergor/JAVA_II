package ru.ifmo.torrent.tracker;

import ru.ifmo.torrent.messages.client_tracker.Marker;
import ru.ifmo.torrent.messages.client_tracker.requests.*;
import ru.ifmo.torrent.messages.client_tracker.response.*;
import ru.ifmo.torrent.messages.Response;
import ru.ifmo.torrent.tracker.state.FileInfo;
import ru.ifmo.torrent.tracker.state.SeedInfo;
import ru.ifmo.torrent.tracker.state.TrackerState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
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
                        InetAddress address = InetAddress.getByName(clientSocket.getInetAddress().getHostAddress());
                        SeedInfo newSeed = new SeedInfo(request.getClientPort(), address);
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
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean update(List<Integer> fileIds, SeedInfo newSeed) {
        Set<Integer> allFiles = trackerState.getAvailableFiles().stream()
            .map(FileInfo::getId)
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
