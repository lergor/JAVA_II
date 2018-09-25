package ru.ifmo.git.util;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListOfFiles {
    public Map<String, String> namesToHashes;

    public void init() {
        namesToHashes = new HashMap<>();
    }

    public void addInfo(String name , String hash) {
        namesToHashes.put(name, hash);
    }
}


