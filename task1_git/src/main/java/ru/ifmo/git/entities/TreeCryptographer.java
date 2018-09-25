package ru.ifmo.git.entities;

import org.apache.commons.io.IOUtils;
import ru.ifmo.git.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

class TreeCryptographer implements Cryptographer {

    private final FileCryptographer fileCrypto = new FileCryptographer();
    private final BlobType type = BlobType.TREE;

    @Override
    public String marker() {
        return type.asString();
    }

    @Override
    public InputStream formContent(Path directory) throws IOException {
        List<Path> files = Files.list(directory).collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();
        for (Path file : files) {
            if(!Files.isHidden(file)) {
                if (Files.isRegularFile(file)) {
                    builder .append(fileCrypto.marker())
                            .append(fileCrypto.getHash(file));
                } else {
                    builder .append(marker())
                            .append(getHash(file));
                }
                builder .append(tab)
                        .append(file.getFileName())
                        .append(sep);
            }
        }
        return IOUtils.toInputStream(builder.toString(), ENCODING);
    }

    public List<FileReference> formEncodeReferences(Path tree) throws IOException {
        List<FileReference> references = new ArrayList<>();
        List<Path> files = Files.list(tree).collect(Collectors.toList());
        references.add(formEncodeReference(tree));
        for (Path file : files) {
            if(!Files.isHidden(file)) {
                if(Files.isRegularFile(file)) {
                    FileReference lll = fileCrypto.formEncodeReference(file);
                    references.add(lll);
                } else {
                    references.addAll(formEncodeReferences(file));
                }
            }
        }
        return references;
    }

    private List<FileReference> decodeComponent(String component, GitFileKeeper storage) throws IOException {
        BlobType type = readMarker(component);
        String[] HashAndName = removeMarker(component).split(tab);
        Path encodedFile = storage.correctPath(HashAndName[0]);
        if(type.equals(BlobType.FILE)) {
            return Collections.singletonList(fileCrypto.decodeFile(encodedFile));
        }
        if(type.equals(BlobType.TREE)) {
            return decodeTree(encodedFile, storage);
        }
        return Collections.emptyList();
    }


    public List<FileReference> decodeTree(Path treeFile,  GitFileKeeper storage) throws IOException {
        List<FileReference> decodedFiles = new ArrayList<>();
        FileReference decodedTree = fileCrypto.decodeFile(treeFile);
        decodedFiles.add(decodedTree);
        List<String> components = IOUtils.readLines(decodedTree.content, ENCODING);
        for (String component: components) {
            List<FileReference> references = decodeComponent(component, storage);
            for(FileReference ref : references) {
                ref.name = Paths.get(decodedTree.name, ref.name).toString();
            }
            decodedFiles.addAll(references);
        }
        return decodedFiles;
    }

}
