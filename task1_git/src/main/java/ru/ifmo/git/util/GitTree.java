package ru.ifmo.git.util;

import java.nio.file.Paths;

public class GitTree {

    static public String cwd() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }

    static public String repo() {
        return Paths.get(cwd(), ".m_git").toString();
    }

    static public String index() {
        return Paths.get(repo(), "index").toString();
    }

    static public String log() {
        return Paths.get(repo(), "logs").toString();
    }

    static public String storage() {
        return Paths.get(repo(), "storage").toString();
    }

    static public String head() {
        return Paths.get(repo(), "HEAD").toString();
    }

}
