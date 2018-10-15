# LGit

### Description

This repository contains simple draft of **git**.

### Usage

The following commands are available:<br/>
```
init
add <files>
rm <files>
status
commit <message> <files>
reset <to_revision>
log [from_revision]
checkout <revision>
checkout -r <files>
```

where *&lt;smth&gt;* means mandatory argument while *[smth]* 
implies he optional one.<br/>
*revision* is represented as short (at least 7 symbols) or entire hash code.<br/>
*checkout -r <files>* is the replacement of *checkout -- <files>*.<br/>

### File hierarchy

    .l_git/
        logs/
            [log_file_for_branch]
        storage/
            [dir_as_hashcode]
                [file]
        index/
            [file]
         HEAD - file with information about current state


### How to build and run

In order to run this app, follow this steps:

* clone this repository 
* checkout on branch *git_milestone_2*
* run `./gradlew installDist`
* go to the `./build/install/l_git/bin/`
* execute `l_git` (or `l_git.bat` on Windows)