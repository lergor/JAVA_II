package ru.ifmo.torrent.messages.client_tracker.requests;

import ru.ifmo.torrent.messages.TorrentMessage;
import ru.ifmo.torrent.messages.TorrentResponse;
import ru.ifmo.torrent.messages.client_tracker.Marker;
import ru.ifmo.torrent.messages.client_tracker.ClientRequest;
import ru.ifmo.torrent.messages.client_tracker.response.UploadResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploadRequest extends ClientRequest implements TorrentMessage {
    private String fileName;
    private long fileSize;

    public UploadRequest() {}

    public UploadRequest(Path file) throws IOException {
        fileName = file.getFileName().toString();
        fileSize = Files.size(file);
    }

    @Override
    public byte marker() {
        return Marker.UPLOAD;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(marker());
        out.writeUTF(fileName);
        out.writeLong(fileSize);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        fileName = in.readUTF();
        fileSize = in.readLong();
    }

    @Override
    public TorrentResponse execute() {
//        System.out.println("doing upload " + fileName + " from "+ client.inetAddress() + " " + client.port());
        return new UploadResponse(trackerState.addFile(fileName, fileSize));
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }
}
