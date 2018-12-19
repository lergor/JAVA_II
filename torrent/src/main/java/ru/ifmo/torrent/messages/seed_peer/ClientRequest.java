package ru.ifmo.torrent.messages.seed_peer;

import ru.ifmo.torrent.messages.TorrentRequest;

public abstract class ClientRequest extends TorrentRequest {
    public static class Marker {
        public static final byte STAT = 1;
        public static final byte GET = 2;
    }

    private static class Command {
        static final String STAT = "stat";
        static final String GET = "get";
        static final String EXIT = "exit";
    }
}
