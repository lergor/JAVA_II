//package ru.ifmo.git.executors;
//
//import ru.ifmo.git.commands1.GitCommand;
//import ru.ifmo.git.commands1.Init;
//import ru.ifmo.git.entities.GitManager;
//import ru.ifmo.git.util.CommandResult;
//import ru.ifmo.git.util.ExitStatus;
//import ru.ifmo.git.util.HeadInfo;
//import ru.ifmo.git.util.Message;
//
//import java.io.IOException;
//
//public class InitExecutor implements GitExecutor {
//
//    @Override
//    public CommandResult execute(GitManager manager, GitCommand command) {
//        Message message = new Message();
//        if (!manager.tree().exists()) {
//            try {
//                manager.tree().createGitTree();
//                message.write("initialized empty ");
//                manager.clerk().writeHeadInfo(new HeadInfo());
//            } catch (IOException e) {
//                String msg = "unable to create repository in " + tree.repo();
//                return new CommandResult(ExitStatus.FAILURE, msg);
//            }
//        } else {
//            message.write("reinitialized existing ");
//        }
//        message.write("lGit repository in " + tree.repo());
//        return new CommandResult(ExitStatus.SUCCESS, message);
//    }
//}
