# Plumbing and Porcelain

![Content image](./../../Program Files/Typora/assets/image-19.png)



## Porcelain

- ```bash
    git init <folder name>
    ```

- ```bash
    git add <file name>
    ```

- ```bash
    git commit -m <message>
    ```

    

## Plumbing

- init .git directory

```bash
git init <folder name>
```



- create a blob object and add to git local repository as a loose object, but never create the file.

 ```bash
    git hash-object -w -t blob <file name>
 ```



- add the blob object into index file, AKA staging the blob

```bash
git update-index --add --cacheinfo <mode> <sha1> <path>
```



- create a tree object and add to git local repository

```bash
git write-tree
```



- create a commit object, or wrapping the tree object, and add it to git local repository

```bash
git commit-tree [(-p <parent sha1>)] <sha1>
```

 

- update the refs: <active branch> and HEAD, taking master as example

```bash
git update-ref refs/heads/master <commit sha1>
git symbolic-ref HEAD refs/heads/master
```



# Git Objects



## Blob

create by git hash-object command, if -w flag is specifiied, the object will write into git local repository.

use git update-index -add -cacheinfo <mode> <>blob sha1> <file name> to stage

## Tree



## Commit



## Tag