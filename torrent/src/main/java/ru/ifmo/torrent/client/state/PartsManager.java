package ru.ifmo.torrent.client.state;

import ru.ifmo.torrent.client.ClientConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PartsManager {

    private final Path storage;

    public PartsManager(Path storage) {
        this.storage = storage;
        if (Files.notExists(storage)) {
            storage.toFile().mkdirs();
        }
    }

    public void storeSplitted(int fileId, Path targetFile) throws IOException {
        InputStream is = Files.newInputStream(targetFile);
        int partNumber = 0;
        byte[] buf = new byte[ClientConfig.FILE_PART_SIZE];
        while (true) {
            int readed = is.read(buf);
            if(readed == -1) return;
            try(OutputStream out = getForWriting(fileId, partNumber)) {
                out.write(buf, 0, readed);
                partNumber++;
            }
        }
    }

    public void mergeSplitted(int fileId, Path targetFile) throws IOException {
        Path fileDir = storage.resolve(String.valueOf(fileId));
        List<Path> parts = Files.list(fileDir)
            .sorted(Comparator.comparing(this::parsePartName))
            .collect(Collectors.toList());

        OutputStream out = Files.newOutputStream(targetFile, StandardOpenOption.TRUNCATE_EXISTING);
        for (Path p: parts) {
            Files.copy(p, out);
        }
    }

    private int parsePartName(Path path) {
        return Integer.parseInt(path.getFileName().toString());
    }

    public OutputStream getForWriting(int fileId, int part) throws IOException {
        Path partFile = storage.resolve(String.valueOf(fileId)).resolve(String.valueOf(part));
        if(Files.notExists(partFile)) {
            Files.createDirectories(partFile.getParent());
            Files.createFile(partFile);
        }
        return Files.newOutputStream(partFile, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public InputStream getForReading(int fileId, int part) throws IOException {
        Path partFile = storage.resolve(String.valueOf(fileId)).resolve(String.valueOf(part));
        return Files.newInputStream(partFile);
    }

    public boolean fileIsPresent(int fileId) {
        return Files.exists(storage.resolve(String.valueOf(fileId)));
    }

}
