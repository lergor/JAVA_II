package ru.ifmo.torrent.tracker;

import ru.ifmo.torrent.messages.TorrentResponse;
import ru.ifmo.torrent.messages.client_tracker.ClientRequest;
import ru.ifmo.torrent.tracker.state.SeedInfo;
import ru.ifmo.torrent.tracker.state.TrackerState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket client;
    private final TrackerState trackerState;

    public ClientHandler(Socket client, TrackerState trackerState) {
        this.client = client;
        this.trackerState = trackerState;
    }

    @Override
    public void run() {
        try (Socket clientSocket = client) {
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());

            int marker;
            while ((marker = in.read()) != -1) {
                ClientRequest request = ClientRequest.fromMarker((byte) marker);
                if (request != null) {
                    request.setTrackerEntities(trackerState, new SeedInfo((short) client.getPort(), client.getInetAddress()));
                    request.read(in);
                    TorrentResponse response = request.execute();
                    response.write(out);
                    out.flush();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("cannot open client socket's stream", e);
        }
    }

//    private void list(ObjectOutputStream out) throws IOException {
//        writeObject(out, new ListResponse(new ArrayList<>(fileIdToFileInfo.values())));
//    }
//
//    private void upload(UploadRequest request, ObjectOutputStream out) throws IOException {
//        Integer fileId = lastFileId.getAndIncrement();
//        fileIdToFileInfo.put(fileId, new FileInfo(fileId, request.getName(), request.getSize()));
//        fileIdToClientInfo.put(fileId, new HashSet<>());
//        writeObject(out, new UploadResponse(fileId));
//    }
//
//    private void sources(SourcesRequest request, ObjectOutputStream out) throws IOException {
//        long curTime = System.currentTimeMillis();
//        List<ClientInfo> clientInfos = fileIdToClientInfo.get(request.getFileId())
//                .stream()
//                .filter(clientInfo -> isOnline(clientInfoLastUpd.get(clientInfo), curTime))
//                .collect(Collectors.toList());
//        writeObject(out, new SourcesResponse(clientInfos));
//    }
//
//    private void update(UpdateRequest request, ObjectOutputStream out) throws IOException {
//        ClientInfo clientInfo = new ClientInfo(client.getInetAddress().getAddress(),
//                request.getClientDataInfo().getClientPort());
//        clientInfoLastUpd.put(clientInfo, System.currentTimeMillis());
//        boolean result = request.getClientDataInfo().getFilesId().stream().allMatch(id -> {
//            if (fileIdToFileInfo.containsKey(id)) {
//                fileIdToClientInfo.get(id).add(clientInfo);
//                return true;
//            }
//            return false;
//        });
//        writeObject(out, new UpdateResponse(result));
//    }
}
