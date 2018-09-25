package ru.ifmo.git;

import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import ru.ifmo.git.commands.GitCommand;
import ru.ifmo.git.entities.GitParser;
import ru.ifmo.git.util.*;

public class Git {

    public static void main(String[] args) {

//            String hash = "b4c66cf0e6c74712a4fd1344ff90ec52855430320";
//            Optional<Path> commit = storage.findCommit(hash);
//////
//            if(commit.isPresent()) {
//                System.out.println("-----");
//                List<FileReference> refs = cryp.formDecodeReferences(commit.get());
//                for (FileReference i : refs) {
//                    System.out.println(i.name);
//                }
//                storage.restoreCommit(refs, clear);
//            }


            ///////////////////
//            FileInfo info = GitCryptographer.decodeFile(file);
//            System.out.println("-----");
//            System.out.println("type: " + String.valueOf(info.type));
//            System.out.println("name: " + info.name);
//            System.out.println("name: " + info.localPath);
//            info.content.forEach(s -> System.out.println(s + "!"));
//
//
//            List<FileInfo> infos = cryp.decodeTree(file);
//            for (FileInfo i: infos) {
//                System.out.println("type: " + String.valueOf(i.type));
//                System.out.println("name: " + i.name);
//                System.out.println("name: " + i.localPath);
//            }

//            Path new_file = Paths.get("kek/res");
//            Files.write(new_file, info.content, StandardCharsets.UTF_8);


            ////////////////////////////////



//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        GitСlerk cl = new GitСlerk(tr);




//        args = new String[]{"init"};
        GitParser gitParser = new GitParser();
        try {
            Namespace ns = gitParser.parseArgs(args);
            GitCommand command = (GitCommand) ns.getAttrs().remove("cmd");
            CommandResult result = command.execute(ns.getAttrs());
            if(result.getStatus() != ExitStatus.SUCCESS) {
                System.out.println("exit with code " + result.getStatus());
            }
            System.out.print(result.getMessage().read());
        } catch (ArgumentParserException e) {
            gitParser.handleError(e);
        }
    }

}
