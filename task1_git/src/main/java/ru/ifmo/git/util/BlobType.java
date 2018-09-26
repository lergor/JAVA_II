package ru.ifmo.git.util;

public enum BlobType {

    FILE, TREE, COMMIT;

    static public BlobType typeOf(String mark) {
        return
                mark.equals("cm\\") ? COMMIT :
                        mark.equals("tr\\") ? TREE :
                                FILE;
    }

    public String asString() {
        switch (this) {
            case FILE:
                return "fl\\";
            case TREE:
                return "tr\\";
            case COMMIT:
                return "cm\\";
        }
        return "";
    }

    static public int size() {
        return 3;
    }
}
