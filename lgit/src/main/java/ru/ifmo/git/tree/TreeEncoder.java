package ru.ifmo.git.tree;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import ru.ifmo.git.entities.GitFileManager;
import ru.ifmo.git.util.BlobType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeEncoder {

    private static final String ENCODING = "UTF-8";
    private static final String sep = System.lineSeparator();
    private static final int markerLength = BlobType.size();
    private static final String tab = "\t";

    private Path storage;

    public static String getFileHash(Path file)
            throws IOException {
        return DigestUtils.sha1Hex(
                new SequenceInputStream(
                        Files.newInputStream(file),
                        IOUtils.toInputStream(file.getFileName().toString(), ENCODING)
                )
        );
    }

    public static String getDirectoryHash(String directoryName, List<Tree> children) throws IOException {
        StringBuilder builder = new StringBuilder();
        children.forEach(c -> builder.append(c.info()));
        return DigestUtils.sha1Hex(
                new SequenceInputStream(
                        IOUtils.toInputStream(builder.toString(), ENCODING),
                        IOUtils.toInputStream(directoryName)
                )
        );
    }

    private static BlobType readMarker(String infoString) {
        return BlobType.typeOf(infoString.substring(0, markerLength));
    }

    private static String removeMarker(String string) {
        return string.substring(markerLength);
    }

    public static String withoutMarker(String infoString) {
        return infoString.substring(markerLength);
    }

    public TreeEncoder(Path storage) {
        this.storage = storage;
    }

    public Tree decode(Path file) throws IOException {
        BufferedReader reader = Files.newBufferedReader(file);
        String infoLine = reader.readLine();
        BlobType type = readMarker(infoLine);
        if (type.equals(BlobType.FILE)) {
            return decodeFile(file);
        } else if (type.equals(BlobType.DIRECTORY)) {
            return decodeTree(file);
        }
        return decodeCommit(file);
    }

    public Tree decode(String commit) throws IOException {
        Path commitFile = GitFileManager.pathInStorage(storage, commit);
        return decode(commitFile);
    }

    private static TreeFile decodeFile(Path encodedFile) throws IOException {
        BufferedReader reader = Files.newBufferedReader(encodedFile);
        String infoLine = reader.readLine();
        String[] HashAndName = withoutMarker(infoLine).split(tab);

        TreeFile tree = new TreeFile();
        tree.setType(readMarker(infoLine));
        tree.setHash(HashAndName[0]);
        tree.setPath(HashAndName.length > 1 ? HashAndName[1] : "");
        tree.setContent(IOUtils.toInputStream(IOUtils.toString(reader)));

        return tree;
    }

    private Tree decodeTree(Path encodedFile) throws IOException {
        TreeFile decoded = decodeFile(encodedFile);

        TreeDirectory tree = new TreeDirectory();
        tree.setType(decoded.type());
        tree.setHash(decoded.hash());
        tree.setPath(decoded.path());

        List<?> components = IOUtils.readLines(decoded.content(), ENCODING);
        tree.setChildren(decodeComponents(components));
        return tree;
    }

    private Tree decodeCommit(Path encodedFile) throws IOException {
        BufferedReader reader = Files.newBufferedReader(encodedFile);
        String infoLine = reader.readLine();

        TreeDirectory tree = new TreeDirectory();
        tree.setType(readMarker(infoLine));
        tree.setHash(withoutMarker(infoLine).split(tab)[0]);
        tree.setPath("");

        List<Tree> children = decodeComponents(Collections.singletonList(reader.readLine()));
        tree.setChildren(children);
        return tree;

    }

    private List<Tree> decodeComponents(List<?> components) throws IOException {
        List<Tree> children = new ArrayList<>();
        for (Object component : components) {
            children.add(decodeComponent((String) component));
        }
        return children;
    }

    private Tree decodeComponent(String component) throws IOException {
        String hash = removeMarker(component).split(tab)[0];
        Path encodedFile = storage.resolve(GitFileManager.pathInStorage(storage, hash));
        return decode(encodedFile);
    }
}
