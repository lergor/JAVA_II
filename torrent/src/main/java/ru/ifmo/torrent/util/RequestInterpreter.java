package ru.ifmo.torrent.util;

import ru.ifmo.torrent.messages.TorrentRequest;

public interface RequestInterpreter {

    TorrentRequest interpret();
}
