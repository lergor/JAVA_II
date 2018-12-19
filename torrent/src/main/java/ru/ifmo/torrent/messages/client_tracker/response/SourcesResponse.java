package ru.ifmo.torrent.messages.client_tracker.response;

import ru.ifmo.torrent.messages.client_tracker.TrackerResponse;
import ru.ifmo.torrent.tracker.state.SeedInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SourcesResponse extends TrackerResponse {
    private int fileID;
    private List<SeedInfo> clients;

    public SourcesResponse() {
        clients = new ArrayList<>();
    }

    public SourcesResponse(int fileID, List<SeedInfo> clients) {
        this.fileID = fileID;
        this.clients = clients;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(clients.size());
        System.out.println("clients.size() " + clients.size());
        for (SeedInfo c : clients) {
            for (byte b : c.IP()) {
                out.write(b);
            }
            out.writeShort(c.port());
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        int count = in.readInt();
        System.out.println("count " + count);

        for (int i = 0; i < count; i++) {
            byte[] IP = new byte[4];
            for (int j = 0; j < 4; j++) {
                IP[j] = in.readByte();
            }
            short port = in.readShort();
            clients.add(new SeedInfo(port, IP));
        }
    }

    @Override
    public void print(PrintStream printer) {
        printer.printf("sources count: %d%n", clients.size());
        for (SeedInfo source : clients) {
            printer.printf("\taddress: %s, port: %d%n", source.inetAddress(), source.port());
        }
    }
}
