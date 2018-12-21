package ru.ifmo.torrent.messages;

import java.io.PrintStream;

public abstract class TorrentResponse implements TorrentMessage {

    public abstract void printTo(PrintStream printer);

}
