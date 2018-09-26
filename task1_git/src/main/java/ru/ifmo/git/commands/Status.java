package ru.ifmo.git.commands;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Status implements GitCommand {

    private GitAssembly git;

    public Status() {
        git = new GitAssembly(GitAssembly.cwd());
    }

    public Status(Path cwd) {
        git = new GitAssembly(cwd);
    }

    @Override
    public boolean correctArgs(Map<String, Object> args) {
        return args.isEmpty();
    }

    @Override
    public CommandResult doWork(Map<String, Object> args) throws GitException {
        if (!git.tree().exists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: not a m_git repository");
        }
        try {
            HeadInfo headInfo = git.clerk().getHeadInfo();
            Message info = new Message();
            info.write("On branch " + headInfo.branchName + GitClerk.sep);
            String changedFiles = getChangedFiles();
            if(changedFiles.isEmpty()) {
                info.write("No changed files");
            } else {
                info.write("Changed files:");
                info.write(changedFiles);
            }
            return new CommandResult(ExitStatus.SUCCESS, info);
        } catch (IOException e) {
            return new CommandResult(ExitStatus.ERROR, e.getMessage());
        }
    }

    private String getChangedFiles() throws IOException {
        StringBuilder builder = new StringBuilder();
        Map<String, String> inIndex = collectNameAndHash(git.tree().index());
        for (Map.Entry<String, String> e : inIndex.entrySet()) {
            Path fileInCWD = git.tree().repo().resolve(e.getKey());
            Path fileInIndex = git.tree().index().resolve(e.getKey());
            if (Files.isRegularFile(fileInIndex)) {
                if (Files.notExists(fileInCWD)) {
                    builder.append(GitClerk.sep).append(e.getKey());
                } else {
                    String hashInIndex = e.getValue();
                    String hashInCWD = git.crypto().getHash(fileInCWD);
                    if (!hashInCWD.equals(hashInIndex)) {
                        builder.append(GitClerk.sep).append(e.getKey());
                    }
                }
            }
        }
        return builder.toString();
    }

    private Map<String, String> collectNameAndHash(Path directory) throws IOException {
        List<Path> files = Files.list(directory).collect(Collectors.toList());
        Map<String, String> nameToHash = new HashMap<>();
        for (Path file : files) {
            String name = file.toFile().getName();
            if (!Files.isHidden(file)) {
                nameToHash.put(name, git.crypto().getHash(file));
                if (Files.isDirectory(file)) {
                    Set<Map.Entry<String, String>> fromSubDir = collectNameAndHash(file).entrySet();
                    for (Map.Entry<String, String> e : fromSubDir) {
                        nameToHash.put(Paths.get(name, e.getKey()).toString(), e.getValue());
                    }
                }
            }
        }
        return nameToHash;
    }
}
