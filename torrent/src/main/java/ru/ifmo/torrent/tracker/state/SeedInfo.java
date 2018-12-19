package ru.ifmo.torrent.tracker.state;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SeedInfo {
    private final short port;
    private final InetAddress inetAddress;

    public SeedInfo(short port, byte[] IP) throws UnknownHostException {
        this.port = port;
        this.inetAddress = InetAddress.getByAddress(IP);
    }

    public SeedInfo(short port, InetAddress inetAddress) {
        this.port = port;
        this.inetAddress = inetAddress;

    }

    public byte[] IP() {
        return inetAddress.getAddress();
    }

    public short port() {
        return port;
    }

    public InetAddress inetAddress() {
        return inetAddress;
    }
}
