package ru.ifmo.git.structs;

import ru.ifmo.git.util.BlobType;

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

    public void setContent(StringBuilder content) {
        this.content = content;
    }

    public void setType(BlobType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

}
