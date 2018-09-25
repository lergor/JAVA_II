package ru.ifmo.git.entities;

import ru.ifmo.git.util.BlobType;

import java.io.*;
import java.nio.file.*;

class FileCryptographer implements Cryptographer {

    private static final BlobType type = BlobType.FILE;

    @Override
    public String marker() {
        return type.asString();
    }

    @Override
    public InputStream formContent(Path file) throws IOException {
        return Files.newInputStream(file);
    }

}
