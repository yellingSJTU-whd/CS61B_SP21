# GitletService

## init()

- Check whether .gitlet exists. If not, do the following:

    - ```java
        Repository.buildGitletRepository()
        ```

        

    - ```java
        createCommit(parent: null, message: "initial commit", date: unix epoch, blobs: null)
        ```



## add()

