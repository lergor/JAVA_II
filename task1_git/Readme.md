# Git

### Description

This repository contains simple draft of **git** 
with the following commands:<br/>
```
init
add <file>
commit <message> <files>
reset <from_commit>
log [from_commit]
checkout <commit>
```
where *&lt;smth&gt;* means mandatory argument while *[smth]* 
implies he optional one.<br/>
*commit* is represented as short (at least 7 symbols) or entire hash code.


### Usage

### File hierarchy

    /* *
     *
     * .m_git/
     *   logs/
     *       {txt_log_files_for_branches}
     *   storage/
     *       {dir_as_hashcode}
     *           {files}
     *   index/
     *       {files}
     *   HEAD - file with information about current state
     *
     * */
