package ru.ifmo.git.util;

public class CommandResult {

    private ExitStatus status;
    private Message message;
    private Throwable error;

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

    public CommandResult(ExitStatus status, String message, Throwable error) {
        this.status = status;
        this.message = new Message(message);
        this.error = error;
    }

    public ExitStatus getStatus() {
        return status;
    }

    public void print() {
        if (status != ExitStatus.SUCCESS) {
            System.out.println("Exit with code " + status);
        }
        System.out.println(message.read());
        if(error != null) {
            error.printStackTrace();
        }
    }

}
