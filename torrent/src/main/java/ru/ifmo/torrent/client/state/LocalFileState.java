package ru.ifmo.torrent.client.state;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LocalFileState {
    private final int fileId;
    private final int numberOfParts;
    private final Set<Integer> readyParts;

    private LocalFileState(int fileId, int numberOfParts, Set<Integer> readyParts) {
        this.fileId = fileId;
        this.numberOfParts = numberOfParts;
        this.readyParts = readyParts;
    }

    public static LocalFileState createEmpty(int fileId, int numberOfParts) {
        return new LocalFileState(fileId, numberOfParts, new HashSet<>());
    }

    public static LocalFileState createFull(int fileId, int numberOfParts) {
        Set<Integer> readyParts = IntStream.range(0, numberOfParts - 1).boxed().collect(Collectors.toSet());
        return new LocalFileState(fileId, numberOfParts, readyParts);
    }

    public static LocalFileState createPartly(int fileId, int numberOfParts, Set<Integer> readyParts) {
        return new LocalFileState(fileId, numberOfParts, readyParts);
    }

    public int getFileId() {
        return fileId;
    }

    public int getNumberOfParts() {
        return numberOfParts;
    }

    public List<Integer> getReadyParts() {
        return new ArrayList<>(readyParts);
    }

    public void addReadyPart(int part) {
        if (part < numberOfParts) {
            readyParts.add(part);
        }
    }

    public static LocalFileState readFrom(DataInputStream in) throws IOException {
        int Id = in.readInt();
        int numOfParts = in.readInt();
        int numOfReadyParts = in.readInt();
        Set<Integer> readyParts = new HashSet<>();
        for (int i = 0; i < numOfReadyParts; i++) {
            int part = in.readInt();
            readyParts.add(part);
        }
        return new LocalFileState(Id, numOfParts, readyParts);
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeInt(fileId);
        out.writeInt(numberOfParts);
        out.writeInt(readyParts.size());
        for (Integer part : readyParts) {
            out.writeInt(part);
        }
        out.flush();
    }
}
