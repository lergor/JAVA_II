package ru.ifmo.git.util;

public enum BlobType {

    FILE, TREE, COMMIT, PARENT_BRANCH;

    static public BlobType typeOf(String mark) {
        return
                mark.equals("cm\\") ? COMMIT :
                        mark.equals("tr\\") ? TREE :
                                mark.equals("fl\\") ? FILE :
                                PARENT_BRANCH;
    }

    public String asString() {
        switch (this) {
            case FILE:
                return "fl\\";
            case TREE:
                return "tr\\";
            case COMMIT:
                return "cm\\";
            case PARENT_BRANCH:
                return "pb\\";
        }
        return "";
    }

    static public int size() {
        return 3;
    }
}
