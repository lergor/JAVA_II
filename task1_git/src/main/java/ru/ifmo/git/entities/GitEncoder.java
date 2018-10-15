package ru.ifmo.git.entities;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import ru.ifmo.git.util.BlobType;
import ru.ifmo.git.util.CommitInfo;
import ru.ifmo.git.util.FileReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GitEncoder {

    private static final String ENCODING = "UTF-8";
    private static final String sep = System.getProperty("line.separator");

    private static String marker(Path path) {
        if (Files.isRegularFile(path)) {
            return BlobType.FILE.asString();
        }
        return BlobType.TREE.asString();
    }

    static String getHash(Path file, Path root) throws IOException {
        InputStream fileContent = formContent(file, root);
        String filePath = file.toFile().getName();
        fileContent = new SequenceInputStream(
                fileContent,
                IOUtils.toInputStream(filePath)
        );
        return DigestUtils.sha1Hex(fileContent);
    }

    private static InputStream formContent(Path path, Path root) throws IOException {
        if (Files.isRegularFile(path)) {
            return Files.newInputStream(path);
        }
        List<Path> files = Files.list(path).collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();
        for (Path file : files) {
            if (!file.toFile().getName().equals(".l_git")) {
                writeInfoString(builder, file, root);
            }
        }
        return IOUtils.toInputStream(builder.toString(), ENCODING);
    }

    private static void writeInfoString(StringBuilder builder, Path file, Path root) throws IOException {
        String tab = "\t";
        builder.append(marker(file))
                .append(getHash(file, root))
                .append(tab)
                .append(root.relativize(file))
                .append(sep);
    }

    private static InputStream formHeader(Path file, Path root) throws IOException {
        String info = marker(file) + root.relativize(file) + sep;
        return IOUtils.toInputStream(info, ENCODING);
    }

    private static InputStream encodeFile(Path file, Path root) throws IOException {
        return new SequenceInputStream(
                formHeader(file, root),
                formContent(file, root)
        );
    }

    private static FileReference formEncodeReference(Path file, Path root) throws IOException {
        FileReference reference = new FileReference();
        reference.type = BlobType.typeOf(marker(file));
        reference.name = getHash(file, root);
        reference.content = encodeFile(file, root);
        return reference;
    }

    private static List<FileReference> formEncodeReferences(Path file, Path root) throws IOException {
        if (Files.isRegularFile(file)) {
            return Collections.singletonList(formEncodeReference(file, root));
        } else {
            List<FileReference> references = new ArrayList<>();
            List<Path> files = Files.list(file).collect(Collectors.toList());
            references.add(formEncodeReference(file, root));
            for (Path f : files) {
                if (Files.isRegularFile(f)) {
                    FileReference lll = formEncodeReference(f, root);
                    references.add(lll);
                } else {
                    references.addAll(formEncodeReferences(f, root));
                }
            }
            return references;
        }
    }

    static String createHash() {
        return DigestUtils.sha1Hex(UUID.randomUUID().toString());
    }

    static String createCommitHash(CommitInfo info) {
        String builder = info.time + info.rootDirectory +
                info.author + info.branch;
        return DigestUtils.sha1Hex(builder);
    }

    private static FileReference formCommitReference(String hash, List<Path> files, Path root) throws IOException {
        FileReference commit = new FileReference();
        commit.type = BlobType.COMMIT;
        commit.name = hash;
        commit.content = encodeCommitFiles(files, root);
        return commit;
    }

    private static List<FileReference> formEncodeReferences(List<Path> files, Path root) throws IOException {
        List<FileReference> references = new ArrayList<>();
        for (Path file : files) {
            if (!file.getFileName().toString().equals(".l_git")) {
                references.addAll(formEncodeReferences(file, root));
            }
        }
        return references;
    }

    private static InputStream encodeCommitFiles(List<Path> files, Path root) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(BlobType.COMMIT.asString()).append(sep);
        for (Path file : files) {
            writeInfoString(builder, file, root);
        }
        return IOUtils.toInputStream(builder.toString(), ENCODING);
    }

    static List<FileReference> formCommitReferences(String hash, Path root) throws IOException {
        List<Path> files = Files.list(root).collect(Collectors.toList());
        List<FileReference> references = formEncodeReferences(files, root);
        FileReference commitReference = formCommitReference(hash, files, root);
        references.add(commitReference);
        return references;
    }

}
