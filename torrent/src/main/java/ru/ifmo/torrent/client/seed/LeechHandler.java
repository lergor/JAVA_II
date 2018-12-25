package ru.ifmo.torrent.client.seed;

import ru.ifmo.torrent.client.storage.LocalFileReference;
import ru.ifmo.torrent.client.storage.LocalFilesManager;
import ru.ifmo.torrent.messages.Request;
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

    private final Socket leechSocket;
    private final LocalFilesManager filesManager;

    LeechHandler(Socket leecher, LocalFilesManager localFilesManager) {
        this.leechSocket = leecher;
        this.filesManager = localFilesManager;
    }

    @Override
    public void run() {
        try (Socket socket = leechSocket) {
            DataInputStream in = new DataInputStream(leechSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(leechSocket.getOutputStream());

            Response response = null;
            int marker;
            while ((marker = in.read()) != -1) {
                switch (marker) {
                    case Marker.GET: {
                        GetRequest request = (GetRequest) Request.readFromDataInputStream(in, GetRequest.class);
                        InputStream is = getPartForDownloading(request.getFileId(), request.getPart());
                        response = new GetResponse(is);
                        is.close();
                        break;
                    }
                    case Marker.STAT: {
                        StatRequest request = (StatRequest) StatRequest.readFromDataInputStream(in, StatRequest.class);
                        response = new StatResponse(getParts(request.getFileId()));
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
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
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