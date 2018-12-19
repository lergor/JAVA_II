package ru.ifmo.torrent.messages.client_tracker;

import ru.ifmo.torrent.messages.TorrentResponse;
import ru.ifmo.torrent.messages.client_tracker.response.ListResponse;
import ru.ifmo.torrent.messages.client_tracker.response.SourcesResponse;
import ru.ifmo.torrent.messages.client_tracker.response.UploadResponse;

public abstract class TrackerResponse extends TorrentResponse {

    public static TrackerResponse fromMarker(byte marker) {
        switch (marker) {
            case Marker.LIST: return new ListResponse();
            case Marker.UPLOAD: return new UploadResponse();
            case Marker.SOURCES: return new SourcesResponse();
            case Marker.UPDATE: return new UploadResponse();
            default: throw new UnsupportedOperationException();
        }

    }
}
