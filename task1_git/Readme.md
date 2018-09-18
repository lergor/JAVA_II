# Git

### Description

This repository contains simple draft of **git**.

### Usage

The following commands are available:<br/>
```
init
add <file>
commit <message> <files>
reset <to_commit>
log [from_commit]
checkout <commit>
```
where *&lt;smth&gt;* means mandatory argument while *[smth]* 
implies he optional one.<br/>
*commit* is represented as short (at least 7 symbols) or entire hash code.


### File hierarchy

    .m_git/
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
* checkout on branch *task1_git*
* run `./gradlew installDist`
* go to the `./build/install/task1_git/bin/`
* execute `task1_git` (or `task1_git.bat` on Windows)