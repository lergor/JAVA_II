package ru.ifmo.git.masters;

import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.inf.*;
import ru.ifmo.git.commands.*;

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

        Subparser parserAdd = subparsers.addParser("add")
                .setDefault("cmd", new Add())
                .help("Add file contents to the index");
        parserAdd.addArgument("<pathspec>").type(String.class)
                .required(true).nargs("+");

        Subparser parserCommit = subparsers.addParser("commit")
                .setDefault("cmd", new Commit())
                .help("Record changes to the repository");
        parserCommit.addArgument("message").type(String.class);
        parserCommit.addArgument("<pathspec>").type(String.class).nargs("+");

        Subparser parserReset = subparsers.addParser("reset")
                .setDefault("cmd", new Reset())
                .help("Reset current HEAD to the specified state");
        parserReset.addArgument("<commit>").type(String.class)
                .required(true);

        Subparser parserLog = subparsers.addParser("log")
                .setDefault("cmd", new Log())
                .help("Show commit logs");
        parserLog.addArgument("<commit>").type(String.class).nargs("?");

        Subparser parserCheckout = subparsers.addParser("checkout")
                .setDefault("cmd", new Checkout())
                .help("Switch branches or restore working tree files");
        parserCheckout.addArgument("<commit>").type(String.class);

    }

    public Namespace parseArgs(String[] args) throws ArgumentParserException {
        return gitParser.parseArgs(args);
    }

    public void handleError(ArgumentParserException e) {
        gitParser.handleError(e);
    }

}
