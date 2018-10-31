package ru.ifmo.git.structs;

import ru.ifmo.git.util.BlobType;

import java.io.InputStream;

public class FileReference {

    public BlobType type;
    public String name;
    public StringBuilder content;

    public BlobType type() {
        return type;
    }

    public String name() {
        return name;
    }

    public String content() {
        return content.toString();
    }
}
