package ru.ifmo.torrent.messages;

public abstract class TorrentRequest implements TorrentMessage {

    public abstract byte marker();

    public abstract TorrentResponse execute();

}
