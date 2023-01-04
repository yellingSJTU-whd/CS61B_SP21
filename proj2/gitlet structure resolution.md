[TOC]



# GitletService

## init

```bash
java gitlet.Main init
```

- **Check** whether  **".gitlet"** folder exists. 
- If not, do the following:
    - create git repository
    - create a special commit object

```java
if (!Repository.GITLET_DIR.exists()) {
    gitletRepo.buildGitletRepository();
    gitletRepo.makeCommit(parents: null, 
                          date: Instant.EPOCH, 
                          message: "initial commit",
                          blobs: null);
}
```



## add

```bash
java gitlet.Main add [file name]
```

- **Check** whether **[file name]** exists
- create a blob with **[file name]**
- store the blob at **".gitlet/objects/blobs"** folder, allocating it by **SHA1**
- update the **staging area**

```java
Blob blob = gitletRepo.makeBlob(String filePath);
gitletRepo.addBlobToObjectDatabase(blob);
gitletRepo.updateIndex(blob);
```



## commit

```bash
java gitlet.Main commit [message]
```

- **Check** if there is any file to commit, or if there is a commit message.
- create a commit, according to **staging area**
- move **HEAD** and **${activated_branch}** pointer

```java
Commit commit = gitletRepo.makeCommit(String message);
gitletRepo.addCommitToObjectDatabase(commit);
gitletRepo.updateRef(String sha1: commit.sha1);
```



## rm

```bash
java gitlet.Main rm [file name]
```

- lookup **index** for status of the file

- switch case: file status

    - tracked, commit tree ≠ staging area = working tree 
        ==> staged for addition 
        ==> change staging area to commit tree, unstaging the changes since last commit

    - tracked, commit tree = staging area ≠ working tree 
        ==> tracked by tip of an branch, and hasn't stage for addition since last commit
        ==> marked for removal at **index**, and delete from working tree
        
    - file tracked, and there are modifications since last commit, which hasn't add or commit
        
    - untracked, not in **index**
        ==> error
    
        ```java
        Utils.error("No reason to remove the file.");
        ```



## log

- fetch **${activated_branch}** from **HEAD**, and fetch the commit at the tip of **${activated_branch}**
- print commits

```java
String activatedBranch = gitletRepo.readSymbolicRef(String symbol: "HEAD");
String sha1 = Utils.readContensAsString(Utils.join(Repository.REF_DATABASE, "heads", activatedBranch));
Commit commit = Utils.readObject(Utils.join(Repository.OBJECT_DATABASE, "commits", sha1), Commit.class);
while(commit != null) {
    gitletRepo.printCommit(commit);
    commit = gitletRepo.fetchFirstParent(commit);
}
```



## global-log

- fetch all commit objects under **.gitlet/objects/commits"** 
- print all commits

```java
List<String> commits = Utils.plainFilenamesIn(Utils.join(Repository.OBJECT_DATABASE, "commits"));

for(String sha1 : commits) {
    File commitFile = Utils.join(Repository.OBJECT_DATABASE, "commits", sha1);
    Commit commit = Utils.readObject(commitFile, Commit.class);
    gitletRepo.printCommit(commit);
}
```



## find

- fetch all commit objects
- filter commits with commit message
- print commits

```java
List<String> commits = Utils.plainFilenamesIn(Utils.join(Repository.OBJECT_DATABASE, "commits"));

for(String sha1: commits) {
    File commitFile = Utils.join(Repository.OBJECT_DATABASE, "commits", sha1);
    Commit commit = Utils.readObject(commitFile, Commit.class);
    if (Objects.equal(commit.message, message)) {
        gitletRepo.printCommit(commit);
    }
}
```



## status

```bash
java gitlet.Main status
```

- store a template as a constant in Repository class
- interpolate branches and HEAD into **Branches**
- iterate over **index** to find **Staged Files** and **Removed Files**

```java
//constant in Repository
public static final String template;
static {
    StringBuilder builder = new StringBuilder();
    builder.append("=== Branches ===").append("%n");
    builder.append("%s%n%n");
    
    builder.append("=== Staged Files ===").append("%n");
    builder.append("%s%n%n");
    
    builder.append("=== Removed Files ===").append("%n");
    builder.append("%s%n%n");
    
    builder.append("=== Modifications Not Staged For Commit ===").append("%n");
    builder.append("%s%n%n");
    
    builder.append("=== Untracked Files ===").append("%n");
    builder.append("%s%n%n");
}
```



## checkout

- ```bash
    java gitlet.Main checkout -- [file name]
    ```

    - search **index** for the file

    - do the following if the file presents in **index**, call error() otherwise

        - change its **working tree** to **commit tree** at **index** 
        - overwrite the real file under CWD

        

- ```bash
    java gitlet.Main checkout [commit id] -- [file name]
    ```

    - fetch the specified commit if presents, then find the corresponding blob to the file

    - do the follwing if the blob found, call error() otherwise

        - changes the file at **wokring tree** and **commit tree** according to the blob
        - overwrite the the real

        

- ```bash
    java gitlet.Main checkout [branch name]
    ```

    - fetch the lastest commit at the specified branch
        - change **index** according to the commit
        - overwrite local files(delete if not tracked in the checkouted branch)



## branch

```bash
java gitlet.Main branch [branch name]
```

- create a new file with the [branch name] under **".gitlet/refs"** folder, and stores the commit sha1 at the branch file



## rm-branch

```bash
java gitlet.Main rm-branch [branch name]
```

- check whether [branch name] exists under **".gitlet/refs"**, or whether [branch name] is current branch
- remove [branch name] under **".gitlet/refs"** folder



## reset

```bash
java gitlet.Main reset [commit id]
```

- **check** whether [commit id] exists under **".gitlet/objects/commits"**, or whether untracked files subjugated to **reset** command
- change **".gitlet/index"** for files whose version inconsistent between **${activated branch}** and **[commit id]**, change working tree to **[commit id]**



## merge

```bash
java gitlet.Main merge [branch name]
```



# Repository

handler of the project