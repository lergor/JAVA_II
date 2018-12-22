package ru.ifmo.torrent.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public abstract class Connection implements AutoCloseable {

    private final Socket socket;
    protected final DataInputStream in;
    protected final DataOutputStream out;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    public Connection(InetAddress address, short port) throws IOException {
        this(new Socket(address, port));
    }

    @Override
    public void close() throws IOException {
        out.flush();
        socket.close();
    }
}
