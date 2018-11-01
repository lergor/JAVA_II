package ru.ifmo.git.util;

public enum BlobType {

    FILE, DIRECTORY, COMMIT, BRANCH;

    static public BlobType typeOf(String mark) {
        return
                mark.equals("cm\\") ? COMMIT :
                        mark.equals("tr\\") ? DIRECTORY :
                                mark.equals("fl\\") ? FILE :
                                BRANCH;
    }

    public String asString() {
        switch (this) {
            case FILE:
                return "fl\\";
            case DIRECTORY:
                return "tr\\";
            case COMMIT:
                return "cm\\";
            case BRANCH:
                return "br\\";
        }
        return "";
    }

    static public int size() {
        return 3;
    }

}
