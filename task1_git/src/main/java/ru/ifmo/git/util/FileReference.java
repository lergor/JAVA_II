package ru.ifmo.git.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FileReference {

    public BlobType type;
    public String name;
    public InputStream content;

}
