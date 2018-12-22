package ru.ifmo.torrent.client.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.torrent.client.storage.LocalFileReference;
import ru.ifmo.torrent.client.storage.LocalFilesManager;
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
    private static final Logger logger = LoggerFactory.getLogger(LeechHandler.class);

    private final Socket peerSocket;
    private final LocalFilesManager filesManager;

    LeechHandler(Socket leecher, LocalFilesManager localFilesManager) {
        this.peerSocket = leecher;
        this.filesManager = localFilesManager;
    }

    @Override
    public void run() {
        try (Socket socket = peerSocket) {
            DataInputStream in = new DataInputStream(peerSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(peerSocket.getOutputStream());

            Response response = null;
            int marker;
            while ((marker = in.read()) != -1) {
                switch (marker) {
                    case Marker.GET: {
                        GetRequest request = GetRequest.readFromDataInputStream(in);
                        logger.debug("Serving {}", request);
                        InputStream is = getPartForDownloading(request.getFileID(), request.getPart());
                        response = new GetResponse(is);
                        is.close();
                        break;
                    }
                    case Marker.STAT: {
                        StatRequest request = StatRequest.readFromDataInputStream(in);
                        List<Integer> av = getParts(request.getFileID());
                        logger.debug("parts " + av);
                        response = new StatResponse(av);
                        break;
                    }
                    default:
                        break;
                }

                if (response != null) {
                    response.write(out);
                    out.flush();

                    // FIXME this is a hack to allow client to read part until -1
                    // ideally client should know size of part he's reading
                    // should handle one request in LeecherHandler
                    if (response instanceof GetResponse) {
                        break;
                    }

                    logger.debug("Request is served with response {}", response);
                }
            }

            logger.debug("Client {} is handled", peerSocket);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private InputStream getPartForDownloading(int fileId, int filePart) throws IOException {
        logger.debug("getPartForDownloading for " + fileId + " " + filePart);
        return filesManager.getPartsManager().getForReading(fileId, filePart);
    }

    private List<Integer> getParts(int fileId) {
        logger.debug("getParts for " + fileId);
        LocalFileReference file = filesManager.getFileReference(fileId);
        return file.getReadyParts();
    }
}