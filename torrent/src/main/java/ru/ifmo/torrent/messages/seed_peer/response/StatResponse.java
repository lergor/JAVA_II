package ru.ifmo.torrent.messages.seed_peer.response;

import ru.ifmo.torrent.network.Response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class StatResponse extends Response {

    private List<Integer> availableParts;

    public StatResponse() {}

    public StatResponse(List<Integer> availableParts) {
        this.availableParts = availableParts;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(availableParts.size());
        for (Integer i: availableParts) {
            out.writeInt(i);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        availableParts = new ArrayList<>();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            availableParts.add(in.readInt());
        }
    }

//    @Override
//    public void printTo(PrintStream printer) {
//        printer.printf("parts count: %d%n", availableParts.size());
//        for (Integer i: availableParts) {
//            printer.println(i);
//        }
//    }

    public List<Integer> getAvailableParts() {
        return availableParts;
    }
}
