package ru.ifmo.torrent.messages.seed_peer.response;

import ru.ifmo.torrent.messages.seed_peer.ClientResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

public class GetResponse extends ClientResponse {

    private Path file;

    public GetResponse(Path file) {
        this.file = file;
    }

    @Override
    public void print(PrintStream printer) {

    }

    @Override
    public void write(DataOutputStream out) throws IOException {

    }

    @Override
    public void read(DataInputStream in) throws IOException {

    }
}
