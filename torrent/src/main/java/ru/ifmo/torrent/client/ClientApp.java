package ru.ifmo.torrent.client;

import ru.ifmo.torrent.client.state.LocalFileReference;
import ru.ifmo.torrent.tracker.state.FileInfo;
import ru.ifmo.torrent.tracker.state.SeedInfo;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        PrintStream printer = new PrintStream(System.out);

        printer.print("enter client port: ");
        Short port = getPort(args, scanner);

        Client client = new Client(InetAddress.getLocalHost(), port);
        main_while:
        while (scanner.hasNext()) {
            String command = scanner.next();
                switch (command) {
                    case Command.EXIT: {
                        printer.println("client: shutting down");
                        break main_while;
                    }
                    case Command.LIST: {
                        List<FileInfo> files = client.getAvailableFiles();
                        printer.printf("files count: %d%n", files.size());
                        for (FileInfo f : files) {
                            printer.printf("\t%s\tid: %d, getSize: %d bytes%n", f.getName(), f.getId(), f.getSize());
                        }
                        break;
                    }
                    case Command.UPLOAD: {
                        String path = scanner.next();
                        Path file = Paths.get(path);
                        if (Files.notExists(file)) {
                            printer.println("file '" + file + "' does not exists");
                            break;
                        }
                        int fileId = client.uploadFile(file);
                        printer.printf("file added with id: %d%n", fileId);
                        break;
                    }
                    case Command.SOURCES: {
                        int fileId = scanner.nextInt();
                        List<SeedInfo> sources = client.getFileSources(fileId);
                        printer.printf("sources count: %d%n", sources.size());
                        for (SeedInfo s : sources) {
                            printer.printf("\taddress: %s, port: %d%n", s.inetAddress(), s.port());
                        }
                        break;
                    }
                    case Command.DOWNLOAD: {
                        int fileId = scanner.nextInt();
                        client.downloadFile(fileId);
                        break;
                    }
                    case Command.STATS: {
                        List<LocalFileReference> files = client.getLocalFiles();
                        printer.printf("local files count: %d%n", files.size());
                        for (LocalFileReference f : files) {
                            printer.printf(
                                "\tfile: %s, id: %d, parts: %d/%d%n",
                                f.getName(),
                                f.getFileId(),
                                f.getReadyParts().size(),
                                f.getNumberOfParts()
                            );
                        }
                        break;
                    }

                    default:
                        printer.printf("client: unknown command: %s%n", command);
                        break;
                }
        }
        client.close();
    }

    private static Short getPort(String[] args, Scanner scanner) {
        if (args.length != 0) {
            return Short.parseShort(args[0]);
        } else {
            while (true) {
                if (!scanner.hasNext()) {
                    System.out.println("enter port: ");
                } else if (scanner.hasNextShort()) {
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
        static final String STATS = "stats";
    }
}
