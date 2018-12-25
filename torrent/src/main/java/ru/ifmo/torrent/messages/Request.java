package ru.ifmo.torrent.messages;

import java.io.DataInputStream;
import java.io.IOException;

public abstract class Request implements NetworkMessage {

    public abstract byte marker();

    public abstract Response getEmptyResponse();

    public static Request readFromDataInputStream(DataInputStream in, Class<? extends Request> cls) throws IllegalAccessException, InstantiationException, IOException {
        Request request = cls.newInstance();
        request.read(in);
        return request;
    }

}
