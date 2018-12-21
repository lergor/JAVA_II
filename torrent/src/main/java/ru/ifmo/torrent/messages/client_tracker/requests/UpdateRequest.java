package ru.ifmo.torrent.messages.client_tracker.requests;

import ru.ifmo.torrent.messages.TorrentMessage;
import ru.ifmo.torrent.messages.TorrentResponse;
import ru.ifmo.torrent.messages.client_tracker.Marker;
import ru.ifmo.torrent.messages.client_tracker.ClientRequest;
import ru.ifmo.torrent.messages.client_tracker.response.UpdateResponse;
import ru.ifmo.torrent.tracker.state.FileInfo;
import ru.ifmo.torrent.tracker.state.SeedInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateRequest extends ClientRequest implements TorrentMessage {
    private short clientPort;
    private List<Integer> fileIDs;

    public UpdateRequest() {
        fileIDs = new ArrayList<>();
    }

    public UpdateRequest(short clientPort, List<Integer> fileIDs) {
        this.clientPort = clientPort;
        this.fileIDs = fileIDs;
    }

    @Override
    public byte marker() {
        return Marker.UPDATE;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(marker());
        out.writeShort(clientPort);
        out.writeInt(fileIDs.size());
        for (Integer ID: fileIDs) {
            out.writeInt(ID);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        clientPort = in.readShort();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            int fileID = in.readInt();
            fileIDs.add(fileID);
        }
    }

    @Override
    public TorrentResponse execute() {
        InetAddress inetAddress = Objects.requireNonNull(this.inetAddress, "Inet address must be specified!");

        System.out.println("update for " + inetAddress + " " + clientPort);
        Set<Integer> allFiles = trackerState.getAvailableFiles().stream()
                .map(FileInfo::fileID)
                .collect(Collectors.toSet());

        if (!allFiles.containsAll(fileIDs)) {
            return new UpdateResponse(false);
        }

        for (int ID : fileIDs) {
            trackerState.addNewSeedIfAbsent(ID, new SeedInfo(clientPort, inetAddress));
        }

        return new UpdateResponse(true);
    }

    public short getClientPort() {
        return clientPort;
    }

    public List<Integer> getFileIDs() {
        return fileIDs;
    }
}