package ru.ifmo.git.entities;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import ru.ifmo.git.util.*;

import java.io.*;
import java.nio.file.*;

public interface Cryptographer {

    String ENCODING = "UTF-8";
    int markLength = BlobType.size();
    String sep = System.getProperty("line.separator");
    String tab = "\t";

    String marker();
    InputStream formContent(Path file) throws IOException;

    default BlobType readMarker(String infoString) {
        return BlobType.typeOf(infoString.substring(0, BlobType.size()));
    }

    default String removeMarker(String string) {
        return string.substring(BlobType.size());
    }

    default InputStream formHeader(Path file) throws IOException {
        String info = marker() + file.toFile().getName() + sep;
        return IOUtils.toInputStream(info, ENCODING);
    }

    default String withoutMarker(String infoString) {
        return infoString.substring(markLength);
    }

    default String getHash(Path file) throws IOException {
        InputStream fileContent = formContent(file);
        String filePath = file.toAbsolutePath().normalize().toString();
        fileContent = new SequenceInputStream(
                fileContent,
                IOUtils.toInputStream(filePath)
        );
        return  DigestUtils.sha1Hex(fileContent);
    }

    default InputStream encodeFile(Path file) throws IOException {
        return new SequenceInputStream(
                formHeader(file),
                formContent(file)
        );
    }

    default FileReference decodeFile(Path file) throws IOException {
        FileReference reference = new FileReference();
        BufferedReader reader = Files.newBufferedReader(file);
        String infoLine = reader.readLine();
        reference.type = readMarker(infoLine);
        reference.name = withoutMarker(infoLine);
        reference.content = IOUtils.toInputStream(IOUtils.toString(reader));
        return reference;
    }

    default FileReference formEncodeReference(Path file) throws IOException {
        FileReference reference = new FileReference();
        reference.type = BlobType.typeOf(marker());
        reference.name = getHash(file);
        reference.content = encodeFile(file);
        return reference;
    }

}
