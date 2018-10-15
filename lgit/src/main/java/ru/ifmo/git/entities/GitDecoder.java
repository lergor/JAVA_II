package ru.ifmo.git.entities;

import org.apache.commons.io.IOUtils;

import ru.ifmo.git.util.BlobType;
import ru.ifmo.git.util.FileReference;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GitDecoder {

    private static final String ENCODING = "UTF-8";
    private static final int markLength = BlobType.size();
    private static final String tab = "\t";


    private static BlobType readMarker(String infoString) {
        return BlobType.typeOf(infoString.substring(0, BlobType.size()));
    }

    private static String removeMarker(String string) {
        return string.substring(BlobType.size());
    }

    static List<FileReference> formCommitReferences(Path commitFile, GitFileKeeper storage) throws IOException {
        return decodeTree(commitFile, storage);
    }

    private static String withoutMarker(String infoString) {
        return infoString.substring(markLength);
    }

    private static List<FileReference> decodeTree(Path treeFile, GitFileKeeper storage) throws IOException {
        List<FileReference> references = new ArrayList<>();
        FileReference decodedTree = decodeFile(treeFile);
        List<?> components = IOUtils.readLines(decodedTree.content, ENCODING);
        for (Object component : components) {
            references.addAll(decodeComponent((String) component, storage));
        }
        return references;
    }

    private static FileReference decodeFile(Path file) throws IOException {
        FileReference reference = new FileReference();
        BufferedReader reader = Files.newBufferedReader(file);
        String infoLine = reader.readLine();
        reference.type = readMarker(infoLine);
        reference.name = withoutMarker(infoLine);
        reference.content = IOUtils.toInputStream(IOUtils.toString(reader));
        return reference;
    }

    private static List<FileReference> decodeComponent(String component, GitFileKeeper storage) throws IOException {
        BlobType type = readMarker(component);
        String[] HashAndName = removeMarker(component).split(tab);
        Path encodedFile = storage.correctPath(HashAndName[0]);
        if (type.equals(BlobType.FILE)) {
            return Collections.singletonList(decodeFile(encodedFile));
        }
        if (type.equals(BlobType.TREE)) {
            return decodeTree(encodedFile, storage);
        }
        return Collections.emptyList();
    }

}
