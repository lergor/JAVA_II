package ru.ifmo.torrent.util;

import java.net.Socket;

public abstract class AbstractServer implements AutoCloseable {

    abstract void start();

    abstract Runnable getRequestHandler(Socket client);

}
