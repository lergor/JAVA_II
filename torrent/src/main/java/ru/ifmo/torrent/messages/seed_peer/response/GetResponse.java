package ru.ifmo.torrent.messages.seed_peer.response;

import ru.ifmo.torrent.client.ClientConfig;
import ru.ifmo.torrent.network.Response;

import java.io.*;

public class GetResponse extends Response {

    private byte[] content;

    public GetResponse() {}

    public GetResponse(OutputStream out) throws IOException {
        this.content = new byte[ClientConfig.FILE_PART_SIZE];
        out.write(content);
        out.flush();
    }

    public GetResponse(byte[] content) {
        this.content = content;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.write(content);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        content = new byte[ClientConfig.FILE_PART_SIZE];
        in.read(content);
    }

    public byte[] getContent() {
        return content;
    }
}
