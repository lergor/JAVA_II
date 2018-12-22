package ru.ifmo.torrent.network;

public abstract class Request implements NetworkMessage {

    public abstract byte marker();

    public abstract Response getEmptyResponse();

}
