package ru.ifmo.torrent.messages.client_tracker;

import ru.ifmo.torrent.messages.TorrentRequest;
import ru.ifmo.torrent.messages.client_tracker.requests.ListRequest;
import ru.ifmo.torrent.messages.client_tracker.requests.SourcesRequest;
import ru.ifmo.torrent.messages.client_tracker.requests.UpdateRequest;
import ru.ifmo.torrent.messages.client_tracker.requests.UploadRequest;
import ru.ifmo.torrent.tracker.state.SeedInfo;
import ru.ifmo.torrent.tracker.state.TrackerState;
import ru.ifmo.torrent.util.TorrentException;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

public abstract class ClientRequest extends TorrentRequest {

    protected TrackerState trackerState;
    protected InetAddress inetAddress;
//    protected SeedInfo client;

    public void setTrackerState(TrackerState trackerState) {
        this.trackerState = trackerState;
    }

    public void setClientInfo(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    private static class Command {
        static final String LIST = "list";
        static final String UPLOAD = "upload";
        static final String SOURCES = "sources";
        static final String UPDATE = "update";
        static final String EXIT = "exit";
    }

    public static ClientRequest fromCommand(String command, Scanner scanner) throws TorrentException, IOException {
        switch (command) {
            case Command.LIST:
                return new ListRequest();
            case Command.UPLOAD: {
                String path = scanner.next();
                Path file = Paths.get(path);
                if (Files.notExists(file)) {
                    throw new TorrentException("file '" + file + "' does not exists");
                }
                return new UploadRequest(file);
            }
            case Command.SOURCES: {
                int fileID = scanner.nextInt();
                return new SourcesRequest(fileID);
            }
            case Command.UPDATE: {
                // FIXME
                return new UpdateRequest((short)1111, Arrays.asList(0));
            }
            default: throw new UnsupportedOperationException();
        }
    }

    public static ClientRequest fromMarker(byte marker) {
        switch (marker) {
            case Marker.LIST: return new ListRequest();
            case Marker.UPLOAD: return new UploadRequest();
            case Marker.SOURCES: return new SourcesRequest();
            case Marker.UPDATE: return new UpdateRequest();
            default: throw new UnsupportedOperationException();
        }
    }
}
