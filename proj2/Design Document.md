# Classes and Data Structures

![Two commits and their blobs](./../../../../Program Files/Typora/assets/commits-and-blobs.png)



## Commit

```java
/**
Represents a commit in gitlet, including metadata, references of parents and references of blobs 
within this commit.
*/
Class Commit implements Dumpable{
    static final ZoneId shanghai  = ZoneId.of("Asia/Shanghai");
    static final var formatter = DateTimeFrmatter.ofPattern("dd-MM-yyyy HH:mm:ss");
     final String sha1;
    
    String branch;
    
    String message;
    ZonedDateTime time;
    Set<> parents = new HashSet<String>(2);
    Map<> blobs = new HashMap<String, String>();
    
    //constructor for initial commit
    Commit(){
        this("init", 1970-00-00, null, null, master);
    }
    
    Commit(String message, ZonedDateTime time, List<> parents, Map<> files){
        //init and calculate sha1
    }
    
    hashCode() {
        //return sha1
    }
    
    equals() {
        //judge equality by sha-1 hash
    }
    
    dump() {
        //display content in this commit like real git.
        //git cat-file -p some_sha-1_hash
        //blob sha-1_hash fileName1.txt
        //blob ...
    }
}
```



## Blob

```java
/**
Stores the content of a file.
*/
Class Blob implements Dumpable{
    //evaluate sha1 hash when constructed, or the context of the file many change.
    final String sha1;
    // the high level represent of the blob
    File file;
    
    Blob(){
        //init and calculate sha1
    }
    
    hashCode(){
        //return sha1
    }
    equals(){
        //judge equality by sha1
    }
    
    dump() {
        //display content of this blob like real git
    }
}
```



## Repository

```java
/**
Represents a gitlet repository, handling persistence jobs such as seralization of gitlet objects.
*/
Class Repository{
    //current working directory
    public static final File CWD;
    
    //the .gitlet directory
    public static final File GITLET_DIR;
    
    public static final File INITIAL_COMMIT = new Commit();
    
    //keeps last commits on branches, such as "master" -> some sha1-hash
    private HashMap<String, String> lastCommit
        
     //stores staged blobs, mimicing staging area in real git.
     //file name -> sha1-hash, such as "test.txt" -> some sha1-hash
     private HashMap<String, String> staging;
    
    
    public boolean init() {
        //
    }
}
```



# Algorithms



## init

- check whether ".gitlet" exists, create one if not.
- ".gitlet" structure:
    - objects
        - blobs
        - commits
    - refs
        - heads
            - master
    - logs
        - heads
    - HEAD
    - index



# Persistence



## Storing objects

".gitlet/objects/blobs" for blobs, and ".gitlet/objects/commits" for commits

- content addressable: use sha1 hash as address (concatenate as git: first two character & the rest)
- create a ".gitlet" at "git init"
- create blob objects at "git add", while commit at "git commit", by serialization

- blob objects keep the content of files, while commits keep metadata and some pointers



## Storing branches

- Branches is just a pointer to a commit sha1, kept at ".gitlet/refs/heads/" folder
- Symbolic ref point to a branch normally, or git will be in "detached HEAD" state. Kept at ".git/HEAD"



# Staging area

- index file