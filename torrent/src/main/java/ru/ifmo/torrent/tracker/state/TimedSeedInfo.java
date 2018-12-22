package ru.ifmo.torrent.tracker.state;

import ru.ifmo.torrent.tracker.TrackerConfig;

public class TimedSeedInfo {

    private final SeedInfo seedInfo;
    private final long creationTime;

    public TimedSeedInfo(SeedInfo seedInfo, long time) {
        this.seedInfo = seedInfo;
        this.creationTime = time;
    }

    @Override
    public boolean equals(Object o) {
        return seedInfo.equals(o);
    }

    @Override
    public int hashCode() {
        return seedInfo.hashCode();
    }

    public SeedInfo getSeedInfo() {
        return seedInfo;
    }

    public boolean notAlive(long currentTime) {
        return currentTime - creationTime >= TrackerConfig.TIMEOUT;
    }
}
