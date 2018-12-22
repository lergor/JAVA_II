package ru.ifmo.torrent.messages;

public abstract class Request implements NetworkMessage {

    public abstract byte marker();

    public abstract Response getEmptyResponse();

}
