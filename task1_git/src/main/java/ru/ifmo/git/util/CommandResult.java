package ru.ifmo.git.util;

public class CommandResult {

    private ExitStatus status;
    private Message message;

    public CommandResult(ExitStatus status) {
        this.status = status;
        this.message = new Message();
    }

    public CommandResult(ExitStatus status, Message message) {
        this.status = status;
        this.message = message;
    }

    public CommandResult(ExitStatus status, String message) {
        this.status = status;
        this.message = new Message(message);
    }

    public ExitStatus getStatus() {
        return status;
    }

    public void setStatus(ExitStatus status) {
        this.status = status;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = new Message(message);
    }
}
