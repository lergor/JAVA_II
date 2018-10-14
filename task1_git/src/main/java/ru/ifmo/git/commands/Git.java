package ru.ifmo.git.commands;

import picocli.CommandLine;

@CommandLine.Command(
        name = "l_git",
        subcommands = {
            Init.class,
            Add.class,
            Remove.class,
            Status.class,
            Commit.class,
            Reset.class,
            Log.class,
            Checkout.class
        },
        description = "A version control system created by lergor."
)
public class Git {
    @CommandLine.Option(
            names = {"-h", "--help"},
            help = true,
            description = "Prints the synopsis and a list of the most commonly used commands.")
    boolean isHelpRequested;
}
