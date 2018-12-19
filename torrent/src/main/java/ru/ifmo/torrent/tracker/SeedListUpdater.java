package ru.ifmo.torrent.tracker;

import ru.ifmo.torrent.tracker.state.SeedInfo;
import ru.ifmo.torrent.util.Config;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class SeedListUpdater implements Runnable {

    private ConcurrentHashMap<SeedInfo, Long> clientInfoLastUpd = new ConcurrentHashMap<>();

    @Override
    public void run() {
        while (true) {

        }
    }
}