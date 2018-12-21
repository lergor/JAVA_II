package ru.ifmo.torrent.messages.client_tracker;

import org.junit.Test;
import ru.ifmo.torrent.messages.client_tracker.response.*;
import ru.ifmo.torrent.tracker.state.FileInfo;
import ru.ifmo.torrent.tracker.state.SeedInfo;

import java.io.*;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ResponseTests {

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private DataOutputStream out = new DataOutputStream(baos);

    @Test
    public void testListResponse() throws IOException {
        List<FileInfo> files = Arrays.asList(
                new FileInfo(0, "file_1", 1),
                new FileInfo(1, "file_2", 100),
                new FileInfo(2, "file_3", 17)
        );

        ListResponse sentResponse = new ListResponse(files);
        ListResponse acceptedResponse = new ListResponse();
        sendAndAccept(sentResponse, acceptedResponse);

        assertThat(acceptedResponse.getFiles().size()).isEqualTo(files.size());
        for (int i = 0; i < files.size(); i++) {
            FileInfo f = acceptedResponse.getFiles().get(i);
            assertThat(f.fileID()).isEqualTo(files.get(i).fileID());
            assertThat(f.name()).isEqualTo(files.get(i).name());
            assertThat(f.size()).isEqualTo(files.get(i).size());
        }
    }

    @Test
    public void testSourceResponse() throws IOException {
        List<SeedInfo> seeds = Arrays.asList(
                new SeedInfo((short) 1111, InetAddress.getLocalHost()),
                new SeedInfo((short) 1212, InetAddress.getLocalHost()),
                new SeedInfo((short) 1313, InetAddress.getLocalHost())
        );
        SourcesResponse sentResponse = new SourcesResponse(0, seeds);
        SourcesResponse acceptedResponse = new SourcesResponse();
        sendAndAccept(sentResponse, acceptedResponse);

        assertThat(acceptedResponse.getFileID()).isEqualTo(sentResponse.getFileID());
        assertThat(acceptedResponse.getClients().size()).isEqualTo(seeds.size());
        for (int i = 0; i < seeds.size(); i++) {
            SeedInfo s = acceptedResponse.getClients().get(i);
            assertThat(s.inetAddress()).isEqualTo(seeds.get(i).inetAddress());
            assertThat(s.port()).isEqualTo(seeds.get(i).port());
        }
    }

    @Test
    public void testUpdateResponse() throws IOException {
        UpdateResponse sentResponse = new UpdateResponse(true);
        UpdateResponse acceptedResponse = new UpdateResponse();

        sendAndAccept(sentResponse, acceptedResponse);

        assertThat(acceptedResponse.getResult()).isEqualTo(sentResponse.getResult());
    }

    @Test
    public void testUploadResponse() throws IOException {
        UploadResponse sentResponse = new UploadResponse(17);
        UploadResponse acceptedResponse = new UploadResponse();

        sendAndAccept(sentResponse, acceptedResponse);

        assertThat(acceptedResponse.getFileID()).isEqualTo(sentResponse.getFileID());
    }

    private void sendAndAccept(TrackerResponse sentResponse, TrackerResponse acceptedResponse) throws IOException {
        sentResponse.write(out);
        out.flush();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        acceptedResponse.read(in);
    }

}
