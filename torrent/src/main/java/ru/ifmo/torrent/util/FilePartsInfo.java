package ru.ifmo.torrent.util;

import java.util.List;

public class FilePartsInfo {

    private List<Integer> availableParts;

    public List<Integer> availableParts() {
        return availableParts;
    }

    public void setAvailableParts(List<Integer> l) {
        availableParts = l;
    }
}
