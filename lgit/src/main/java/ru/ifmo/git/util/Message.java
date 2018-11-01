package ru.ifmo.git.util;

public class Message {

    private StringBuilder builder;

    public Message() {
        builder = new StringBuilder();
    }

    public Message(String text) {
        builder = new StringBuilder();
        write(text);
    }

    public void write(String text) {
        builder.append(text);
    }

    public String read() {
        return builder.toString();
    }

}
