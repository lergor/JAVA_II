package ru.ifmo.torrent.client.seed;

import ru.ifmo.torrent.client.state.LocalFilesManager;
import ru.ifmo.torrent.messages.seed_peer.Marker;
import ru.ifmo.torrent.network.Response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class PeerHandler implements Runnable {
    private final Socket peerSocket;
    private final LocalFilesManager localFilesManager;

    PeerHandler(Socket peerSocket, LocalFilesManager localFilesManager) {
        this.peerSocket = peerSocket;
        this.localFilesManager = localFilesManager;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(peerSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(peerSocket.getOutputStream());

            Response response = null;
            int marker;
            while ((marker = in.read()) != -1) {
                switch (marker) {
                    case Marker.GET: break;
                    case Marker.STAT: break;
                    default: break;
                }
//                eerPRequest request = PeerRequest.fromMarker((byte) marker);
//                request.read(in);
//                request.setClientState(localFilesManager);
                response.write(out);

                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}