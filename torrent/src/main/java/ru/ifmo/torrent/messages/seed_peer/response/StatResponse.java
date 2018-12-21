package ru.ifmo.torrent.messages.seed_peer.response;

import ru.ifmo.torrent.messages.seed_peer.ClientResponse;
import ru.ifmo.torrent.util.FilePartsInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class StatResponse extends ClientResponse {

    private FilePartsInfo filePartsInfo;
    // Формат ответа: <count: Int> (<part: Int>)*, count — количество доступных частей part — номер части

    public StatResponse() {
        filePartsInfo = new FilePartsInfo();
    }

    public StatResponse(FilePartsInfo filePartsInfo) {
        this.filePartsInfo = filePartsInfo;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        List<Integer> availableParts = filePartsInfo.availableParts();
        out.writeInt(availableParts.size());
        for (Integer i: availableParts) {
            out.writeInt(i);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        List<Integer> parts = new ArrayList<>();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            parts.add(in.readInt());
        }
        filePartsInfo.setAvailableParts(parts);
    }


    @Override
    public void printTo(PrintStream printer) {
        printer.printf("parts count: %d%n", filePartsInfo.availableParts().size());
        for (Integer i: filePartsInfo.availableParts()) {
            printer.println(i);
        }
    }
}
