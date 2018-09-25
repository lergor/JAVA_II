package ru.ifmo.git.commands;

import ru.ifmo.git.entities.*;
import ru.ifmo.git.util.*;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.*;
import java.util.*;
import java.util.stream.Collectors;

public class Commit implements GitCommand {

    private HeadInfo headInfo;
    private CommitInfo commitInfo;
    private List<Path> files = new LinkedList<>();
    private GitTree gitTree;
    private GitClerk gitClerk;
    private GitFileKeeper gitFileKeeper;
    private GitCryptographer gitCrypto;

    public Commit() {
        initEntities(GitTree.cwd());
    }

    public Commit(Path cwd) {
        initEntities(cwd);
    }

    void initEntities(Path cwd) {
        gitTree = new GitTree(cwd);
        gitClerk = new GitClerk(gitTree);
        gitFileKeeper = new GitFileKeeper(gitTree);
        gitCrypto = new GitCryptographer(gitTree);
    }

    private List<Path> getArgs(Map<String, Object> args) {
        return ((List<String>) args.get("<pathspec>"))
                .stream()
                .map(s -> gitTree.index().resolve(s).normalize())
                .collect(Collectors.toList());
    }

    @Override
    public boolean correctArgs(Map<String, Object> args) throws GitException {
        if (!args.isEmpty()) {
            List<Path> files = getArgs(args);
            checkFilesExist(files);
            this.files = files;
            return true;
        }
        return false;
    }

    @Override
    public CommandResult execute(Map<String, Object> args) {
        try {
            if(!correctArgs(args)) {
                return new CommandResult(ExitStatus.ERROR, "try git add first\n");
            }
            checkRepoAndArgs(args);
            headInfo = FileMaster.getHeadInfo(new File(GitTree.head()));
            setCommitInfo();
            StorageMaster.copyAll(files, Paths.get(GitTree.storage(), headInfo.currentHash).toFile());
            writeLog();
            return new CommandResult(ExitStatus.SUCCESS, "commit: done!\n");
        } catch (GitException e) {
            return new CommandResult(ExitStatus.ERROR, "commit: " + e.getMessage());
        }
    }


    @Override
    public CommandResult doWork(Map<String, Object> args) throws GitException {
        if (!gitTree.exists()) {
            return new CommandResult(ExitStatus.ERROR, "fatal: not a m_git repository");
        }

        
            CommitInfo commitInfo = new CommitInfo();
            commitInfo.setBranch("master");
            commitInfo.setMessage("master?");
            commitInfo.setRootDirectory(tr.repo());
            commitInfo.setAuthor("lergor");
            commitInfo.setTime("1234345678");
            File[] files = tr.repo().toFile().listFiles();
            List<Path> filess = Arrays.stream(files).map(File::toPath).filter(s -> !s.toFile().isHidden()).collect(Collectors.toList());
            GitCryptographer cryp = new GitCryptographer(tr);
            GitFileKeeper storage = new GitFileKeeper(tr.storage());



            //////////////////

            List<FileReference> refs1 = cryp.formEncodeReferences(commitInfo, filess);
            for (FileReference i : refs1) {
                System.out.println(i.name + " " + i.type);
            }
//            storage.saveCommit(refs1);

        return null;
    }


    private void setCommitInfo() throws GitException {
        commitInfo.author = System.getProperty("user.name");
        commitInfo.message = message;
        commitInfo.branch = headInfo.branchName;

        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZ");
        Calendar calendar = Calendar.getInstance();
        commitInfo.time = df.format(calendar.getTime());

        String name = String.valueOf(commitInfo.time.concat(String.valueOf(GitTree.cwd())).hashCode());
        commitInfo.name = (UUID.randomUUID().toString() + name).replaceAll("-", "");

        boolean moveHead = headInfo.headHash.equals(headInfo.currentHash);
        FileMaster.changeCurHash(commitInfo.name, moveHead);
    }

    private void writeLog() throws GitException {
        if (commitInfo.message.isEmpty()) {
            getUserMessage();
        }
        String logContent = (new Gson()).toJson(commitInfo);
        String logFile = Paths.get(GitTree.log(), headInfo.branchName).toString();
        FileMaster.writeToFile(logFile, logContent, true);
    }

    private void getUserMessage() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("please enter message: ");
            commitInfo.message = br.readLine();
        } catch (IOException e) {
            commitInfo.message = "no message";
        }
    }

}
