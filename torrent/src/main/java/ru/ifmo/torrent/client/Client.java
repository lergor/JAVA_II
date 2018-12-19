package ru.ifmo.torrent.client;

import ru.ifmo.torrent.messages.client_tracker.ClientRequest;
import ru.ifmo.torrent.messages.client_tracker.TrackerResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;

public class Client implements AutoCloseable {
    private static final int TRACKER_PORT = 8081;
    private final short port;
    private Socket clientSocket;

    private final DataOutputStream out;
    private final DataInputStream in;

    private byte currentRequest;

    private Path metaDir;

    public Client(InetAddress inetAddress, short port) throws IOException {
        this.port = port;
        clientSocket = new Socket(inetAddress, TRACKER_PORT);
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());
    }

    public boolean sendRequest(ClientRequest request) {
        currentRequest = request.marker();
        try {
            request.write(out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public TrackerResponse getResponse() throws IOException {
        TrackerResponse response = TrackerResponse.fromMarker(currentRequest);
        response.read(in);
        return response;
    }

    public void run() {
        while (true) {
            if(clientSocket.isClosed()) return;

        }
    }

    @Override
    public void close() throws IOException {
        // TODO write state to file
        clientSocket.close();
    }

}