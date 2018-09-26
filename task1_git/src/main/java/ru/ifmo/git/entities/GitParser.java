package ru.ifmo.git.entities;

import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.inf.*;
import ru.ifmo.git.commands.*;

import java.nio.file.Path;

public class GitParser {

    private ArgumentParser gitParser;

    public GitParser() {
        createGitParser();
    }

    private void createGitParser() {
        gitParser = ArgumentParsers.newArgumentParser("git");
        gitParser.defaultHelp(true)
                .description("A version control system created by lergor.");

        Subparsers subparsers = gitParser.addSubparsers()
                .title("These are common Git commands used in various situations:");

        Subparser parserInit = subparsers.addParser("init")
                .setDefault("cmd", new Init())
                .help("Create an empty Git repository or reinitialize an existing one");
        parserInit.addArgument("<directory>")
                .type(Path.class).nargs("?").setDefault(GitAssembly.cwd());

        Subparser parserAdd = subparsers.addParser("add")
                .setDefault("cmd", new Add())
                .help("Add file contents to the index");
        parserAdd.addArgument("<pathspec>").type(String.class)
                .required(true).nargs("+");

        Subparser parserRemove = subparsers.addParser("rm")
                .setDefault("cmd", new Remove())
                .help("Remove files from the working tree and from the index");
        parserRemove.addArgument("<pathspec>").type(String.class)
                .required(true).nargs("+");

        Subparser parserStatus = subparsers.addParser("status")
                .setDefault("cmd", new Status())
                .help("Show current state of the repository");

        Subparser parserCommit = subparsers.addParser("commit")
                .setDefault("cmd", new Commit())
                .help("Record changes to the repository");
        parserCommit.addArgument("-m", "--message").nargs("?").type(String.class);

        Subparser parserReset = subparsers.addParser("reset")
                .setDefault("cmd", new Reset())
                .help("Reset current HEAD to the specified state");
        parserReset.addArgument("<commit>").nargs("?").type(String.class);

        Subparser parserLog = subparsers.addParser("log")
                .setDefault("cmd", new Log())
                .help("Show commit logs");
        parserLog.addArgument("<commit>").type(String.class).nargs("?");

        Subparser parserCheckout = subparsers.addParser("checkout")
                .setDefault("cmd", new Checkout())
                .help("Switch branches or restore working tree files");
        parserCheckout.addArgument("<commit>").nargs("?").type(String.class);

    }

    public Namespace parseArgs(String[] args) throws ArgumentParserException {
        return gitParser.parseArgs(args);
    }

    public void handleError(ArgumentParserException e) {
        gitParser.handleError(e);
    }

}
