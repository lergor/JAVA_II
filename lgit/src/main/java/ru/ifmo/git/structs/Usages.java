package ru.ifmo.git.structs;

import java.util.HashMap;

public class Usages {

    public HashMap<String, Integer> hashToCount = new HashMap<>();

    public void increment(String hash) {
        hashToCount.put(hash, hashToCount.getOrDefault(hash, 0) + 1);
    }

    public boolean decrement(String hash) {
        Integer count = hashToCount.getOrDefault(hash, 0) - 1;
        if (count <= 0) {
            hashToCount.remove(hash);
            return false;
        }
        hashToCount.put(hash, count);
        return true;
    }

}
