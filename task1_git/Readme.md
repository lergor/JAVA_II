# Git

### Description

This repository contains simple draft of **git** 
with the following commands:<br/>
```
init
add <file>
commit <message> <files>
reset <to_revision>
log [from_revision]
checkout <revision>
```
where *&lt;smth&gt;* means mandatory argument while *[smth]* 
implies he optional one.

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
     *   refs/
     *       heads/
     *           {txt_files_with_hashcode_for_head}
     *   info/
     *      {json_file_with_info_for_branch}
     *   HEAD - file with current HEAD
     *
     * */
