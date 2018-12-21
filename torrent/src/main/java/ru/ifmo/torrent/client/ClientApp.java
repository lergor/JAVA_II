package ru.ifmo.torrent.client;

import ru.ifmo.torrent.messages.client_tracker.ClientRequest;
import ru.ifmo.torrent.messages.client_tracker.TrackerResponse;
import ru.ifmo.torrent.util.TorrentException;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        PrintStream printer = new PrintStream(System.out);

        printer.print("enter client port: ");
        Short port = getPort(args, scanner);

        main_while:
        while (scanner.hasNext()) {
            String command = scanner.next();
            ClientRequest request = null;
            try (Client client = new Client(InetAddress.getLocalHost(), port)) {
                switch (command) {
                    case Command.EXIT:
                        printer.println("client: shutting down");
                        break main_while;
                    case Command.LIST:
                    case Command.UPLOAD:
                    case Command.SOURCES:
                    case Command.UPDATE:
                        try {
                            request = ClientRequest.fromCommand(command, scanner);
                        } catch (TorrentException e) {
                            e.write(printer);
                        }
                        break;
                    default:
                        printer.printf("client: unknown command: %s%n", command);
                        break;
                }

                if(request != null) {
                    client.sendRequest(request);
                    TrackerResponse answer = client.getResponse();
                    answer.printTo(printer);
                }
            }
        }

    }

    private static Short getPort(String[] args, Scanner scanner) {
        if (args.length != 0) {
            return Short.parseShort(args[0]);
        } else {
            while (true) {
                if(!scanner.hasNext()) {
                    System.out.println("enter port: ");
                } else if(scanner.hasNextShort()) {
                    return scanner.nextShort();
                }
            }
        }
    }

    private static class Command {
        static final String LIST = "list";
        static final String UPLOAD = "upload";
        static final String SOURCES = "sources";
        static final String UPDATE = "update";
        static final String EXIT = "exit";
        static final String DOWNLOAD = "download";
    }
}
