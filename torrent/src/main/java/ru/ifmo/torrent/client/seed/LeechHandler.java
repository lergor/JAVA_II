package ru.ifmo.torrent.client.seed;

import ru.ifmo.torrent.client.state.LocalFileReference;
import ru.ifmo.torrent.client.state.LocalFilesManager;
import ru.ifmo.torrent.messages.seed_peer.Marker;
import ru.ifmo.torrent.messages.seed_peer.requests.*;
import ru.ifmo.torrent.messages.seed_peer.response.*;
import ru.ifmo.torrent.messages.Response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

class LeechHandler implements Runnable {
    private final Socket peerSocket;
    private final LocalFilesManager filesManager;

    LeechHandler(Socket peerSocket, LocalFilesManager localFilesManager) {
        this.peerSocket = peerSocket;
        this.filesManager = localFilesManager;
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
                    case Marker.GET:  {
                        GetRequest request = GetRequest.readFromDataInputStream(in);
                        InputStream is = getPartForDownloading(request.getFileID(), request.getPart());
                        response = new GetResponse(is);
                        is.close();
                        break;
                    }
                    case Marker.STAT: {
                        StatRequest request = StatRequest.readFromDataInputStream(in);
                        response = new StatResponse(getParts(request.getFileID()));
                        break;
                    }
                    default: break;
                }
                if(response != null) {
                    response.write(out);
                    out.flush();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private InputStream getPartForDownloading(int fileId, int filePart) throws IOException {
        return filesManager.getPartsManager().getForReading(fileId, filePart);
    }

    private List<Integer> getParts(int fileId) {
        LocalFileReference file = filesManager.getFileReference(fileId);
        return file.getReadyParts();
    }
}