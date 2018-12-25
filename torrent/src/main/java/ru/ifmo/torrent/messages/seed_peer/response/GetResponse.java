package ru.ifmo.torrent.messages.seed_peer.response;

import org.apache.commons.io.IOUtils;
import ru.ifmo.torrent.client.ClientConfig;
import ru.ifmo.torrent.messages.Response;

import java.io.*;


public class GetResponse extends Response {

    private byte[] content;

    public GetResponse() {}

    public GetResponse(InputStream in) throws IOException {
        this.content = new byte[ClientConfig.FILE_PART_SIZE];
        in.read(content);
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
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(ClientConfig.FILE_PART_SIZE);
        IOUtils.copy(in, buffer);
        content = buffer.toByteArray();
    }

    public byte[] getContent() {
        return content;
    }
}
