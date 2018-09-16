package ru.ifmo.git.masters;

import org.apache.commons.io.FileUtils;
import ru.ifmo.git.util.*;

import java.io.*;
import java.util.*;

public class StorageMaster {

    static public void copyAll(List<String> args, String sourceDir, String targetDir) throws GitException {
        sourceDir = GitUtils.fixDirName(sourceDir);
        targetDir = GitUtils.fixDirName(targetDir);
        File destination = new File(GitUtils.getCWD() + "/" + targetDir);
        if(destination.exists() || (!destination.exists() && destination.mkdirs())) {
            for (String fileName: args) {
                fileName = GitUtils.fixFileName(fileName);
                copyTo(sourceDir + "/" + fileName, targetDir + "/" + fileName);
            }
        }
    }

    static public void copyDirToDir(String sourceDir, String targetDir) throws GitException {
        File[] files = new File(sourceDir).listFiles();
        if(files != null) {
            List<String> args = new ArrayList<>();
            Arrays.stream(files).map(File::getName).forEach(args::add);
            copyAll(args, sourceDir, targetDir);
        }
    }

    static public void copyTo(String source, String destination) throws GitException {
        File sourceFile = new File(GitUtils.getCWD() + "/" + source);
        if(!sourceFile.isHidden()) {
            try {
                if(sourceFile.isFile()) {
                    File destinationFile = new File(GitUtils.getCWD() + "/"  + destination);
                    FileUtils.copyFile(sourceFile, destinationFile);
                } else if(sourceFile.isDirectory()) {
                    checkAndCreateDir(GitUtils.getCWD() + "/"  + destination);
                    File[] files = sourceFile.listFiles();
                    if(files != null) {
                        for (File file: files) {
                            copyTo(source + "/" + file.getName(), destination + "/" + file.getName());
                        }
                    }
                }
            } catch (IOException e) {
                String errorMessage = "error while moving " + source + " into " + destination + "\n";
                throw new GitException(errorMessage);
            }
        }
    }

    static public boolean createFileWithContent(String fileName, String content) {
        return checkAndCreateFile(fileName) && writeToFile(fileName, content, false);
    }

    static public boolean checkAndCreateDir(String dirName) {
        File dir = new File(dirName);
        return dir.exists() || dir.mkdirs();
    }

    static public boolean checkAndCreateFile(String fileName) {
        File file = new File(fileName);
        if(!file.exists()) {
            try {
                if(file.createNewFile()) {
                    return file.setReadable(true) && file.setWritable(true);
                }
            } catch (IOException e) {
                return false;
            }
        }
        return file.canRead() && file.canWrite();
    }

    static public boolean writeToFile(String fileName, String content, boolean append) {
        File file = new File(fileName);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
            writer.write((append ? "\n" : "") + content);
        } catch (IOException e) {
            return false;
        }
        return true;
    }


}
