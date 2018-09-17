package ru.ifmo.git.masters;

import org.apache.commons.io.FileUtils;
import ru.ifmo.git.util.*;

import java.io.*;
import java.util.*;

public class StorageMaster {

    static public void copyAll(List<File> args, File targetDir) throws GitException {
        if (targetDir.exists() || (!targetDir.exists() && targetDir.mkdirs())) {
            for (File file : args) {
                try {
                    if (file.isFile()) {
                        FileUtils.copyFileToDirectory(file, targetDir);
                    } else if (file.isDirectory()) {
                        FileUtils.copyDirectoryToDirectory(file, targetDir);
                    }
                } catch (IOException e) {
                    throw new GitException(e.getMessage());
                }
            }
        }
    }

    static public boolean createFileWithContent(String fileName, String content) {
        if (checkAndCreateFile(fileName)) {
            try {
                FileUtils.writeStringToFile(new File(fileName), content, "UTF-8");
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkAndCreateFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    return file.setReadable(true) && file.setWritable(true);
                }
            } catch (IOException e) {
                return false;
            }
        }
        return file.canRead() && file.canWrite();
    }

}
