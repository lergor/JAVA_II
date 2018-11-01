package ru.ifmo.git.tree.visitors;

import org.apache.commons.io.IOUtils;
import ru.ifmo.git.entities.GitFileManager;
import ru.ifmo.git.tree.Tree;
import ru.ifmo.git.tree.TreeDirectory;
import ru.ifmo.git.tree.TreeFile;
import ru.ifmo.git.tree.TreeVisitor;
import ru.ifmo.git.structs.FileReference;
import ru.ifmo.git.structs.Usages;
import ru.ifmo.git.util.GitException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class CommitVisitor implements TreeVisitor {

    private List<FileReference> references = new ArrayList<>();

    public CommitVisitor() {
    }

    public void saveReferences(Path storage) throws IOException {
        for (FileReference r : references) {
            Path file = GitFileManager.pathInStorage(storage, r.name);
            if (Files.notExists(file)) {
                if (Files.notExists(file.getParent())) {
                    Files.createDirectories(file.getParent());
                }
                Files.createFile(file);
            }
            Files.copy(IOUtils.toInputStream(r.content.toString()),
                    file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String formCommitContent(Tree tree) {
        if (tree instanceof TreeFile) {
            return ((TreeFile) tree).content();
        }
        StringBuilder builder = new StringBuilder();
        for (Tree child : ((TreeDirectory) tree).children()) {
            builder.append(child.info());
        }
        return builder.toString();
    }

    private FileReference formReference(Tree tree) {
        FileReference reference = new FileReference();
        reference.type = tree.type();
        reference.name = tree.hash();
        reference.content = new StringBuilder()
                .append(tree.info())
                .append(formCommitContent(tree));
        return reference;
    }

    @Override
    public void visit(TreeFile tree) {
        references.add(formReference(tree));
    }

    @Override
    public void visit(TreeDirectory tree) throws IOException, GitException {
        references.add(formReference(tree));
        visit(tree.children());
    }

    public List<FileReference> references() {
        return references;
    }

    public void addReferencesToUsages(Usages usages) {
        references.forEach(r -> usages.increment(r.name));
    }
}
