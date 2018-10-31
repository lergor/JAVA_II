package ru.ifmo.git.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Message {

    private ByteArrayOutputStream os;

    public Message() {
        os = new ByteArrayOutputStream();
    }

    public Message(String text) {
        os = new ByteArrayOutputStream();
        write(text);
    }

    public void write(String text) {
        try (Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writer.write(text);
        } catch (IOException ignored) {
        }
    }

    public String read() {
        return os.toString();
    }

    public void clear() {
        os.reset();
    }

    public void print() {
        System.out.print(read());
    }

    public static String initializedMessage = "";
    public static String reitializedMessage = "";

}
