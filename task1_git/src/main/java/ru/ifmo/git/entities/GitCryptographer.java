package ru.ifmo.git.entities;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import ru.ifmo.git.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class GitCryptographer {

    private final BlobType type = BlobType.COMMIT;
    private static final TreeCryptographer treeCrypto = new TreeCryptographer();
    private static final FileCryptographer fileCrypto = new FileCryptographer();
    private GitTree gitTree;

    public GitCryptographer(GitTree gitTree) {
        this.gitTree = gitTree;
    }

    private String marker() {
        return type.asString();
    }

    public String getHash(Path file) throws IOException {
        if(file.toFile().isFile()) {
            return fileCrypto.getHash(file);
        }
        return treeCrypto.getHash(file);
    }

    public static String createHash() {
        return DigestUtils.sha1Hex(UUID.randomUUID().toString());
    }
    public static String createCommitHash(CommitInfo info) {
        String builder = info.time + info.rootDirectory +
                         info.author + info.branch;
        return DigestUtils.sha1Hex(builder);
    }

    private BlobType getMarker(Path file) {
        if(Files.isDirectory(file)) {
            return BlobType.TREE;
        }
        return BlobType.FILE;
    }

    public InputStream encodeFiles(List<Path> files) throws IOException {
        String sep = System.getProperty("line.separator");
        StringBuilder builder = new StringBuilder();
        builder.append(marker()).append(sep);
        for (Path file : files) {
            builder.append(getMarker(file).asString())
                    .append(getHash(file))
                    .append("\t")
                    .append(file.getFileName())
                    .append(sep);
        }
        return IOUtils.toInputStream(builder.toString(), "UTF-8");
    }

//    private Path getTreePath(List<String> lines) {
//        String[] HashAndName = lines.get(1).split("\t");
//        return Paths.get(withoutMarker(HashAndName[0]));
//    }

    public List<FileReference> formDecodeReferences(Path commitFile) throws IOException {
       return treeCrypto.decodeTree(commitFile, new GitFileKeeper(gitTree));
    }

    public FileReference formHeaderReference(String hash, List<Path> files) throws IOException {
        FileReference commit = new FileReference();
        commit.type = type;
        commit.name = hash;
        commit.content = encodeFiles(files);
        return commit;
    }

    public List<FileReference> formEncodeReferences(List<Path> files) throws IOException {
        List<FileReference> references = new ArrayList<>();
        for (Path file : files) {
            if(!Files.isHidden(file)) {
                if(Files.isDirectory(file)) {
                    references.addAll(treeCrypto.formEncodeReferences(file));
                } else {
                    references.add(fileCrypto.formEncodeReference(file));
                }
            }
        }
        return references;
    }

}
