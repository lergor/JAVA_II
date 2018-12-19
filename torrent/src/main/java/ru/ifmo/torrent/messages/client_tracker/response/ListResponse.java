package ru.ifmo.torrent.messages.client_tracker.response;

import ru.ifmo.torrent.messages.client_tracker.TrackerResponse;
import ru.ifmo.torrent.tracker.state.FileInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ListResponse extends TrackerResponse {
    private List<FileInfo> files;

    public  ListResponse() {
        files = new ArrayList<>();
    }

    public ListResponse(List<FileInfo> files) {
        this.files = files;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(files.size());
        for (FileInfo f: files) {
            out.writeInt(f.fileID());
            out.writeUTF(f.name());
            out.writeLong(f.size());
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            int ID = in.readInt();
            String name = in.readUTF();
            long size = in.readLong();
            FileInfo f = new FileInfo(ID, name, size);
            files.add(f);
        }

    }

    @Override
    public void print(PrintStream printer) {
        printer.printf("files count: %d%n", files.size());
        for (FileInfo f : files) {
            printer.printf("\t%s\tid: %d, size: %d bytes%n", f.name(), f.fileID(), f.size());
        }
    }
}
