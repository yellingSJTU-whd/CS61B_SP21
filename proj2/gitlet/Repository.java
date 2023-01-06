package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

import static gitlet.Utils.*;
import static java.lang.Thread.sleep;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {
    /**
     * Current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * Gitlet repository.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    private HashMap<String, Entry> index;

    private static Repository repository;

    private Repository() {
        //load index if exists
        var indexFile = join(GITLET_DIR, "index");
        if (indexFile.exists() && indexFile.isFile()) {
            index = readObject(indexFile, HashMap.class);
        }
    }

    /**
     * @return an instance of Repository
     */
    public static Repository getInstance() {
        if (repository == null) {
            repository = new Repository();
        }
        return repository;
    }

    /* TODO: fill in the rest of this class. */
    public static final ZoneId shanghai = ZoneId.of("Asia/Shanghai");

    /**
     * An entry represents a file, tracking its status by keeping sha1 under three trees.
     */
    private static class Entry {

        /**
         * The time of last update.
         */
        ZonedDateTime mtime;

        /**
         * Relative path under Current Working Directory.
         */
        String path;

        /**
         * File version in working directory, represented by SHA-1 HASH.
         */
        String wdir;

        /**
         * File version in the index, represented by SHA-1 HASH.
         */
        String stage;

        /**
         * File version in the repository, represented by SHA-1 HASH.
         */
        String repo;

        Entry(ZonedDateTime mtime, String path, String wdir, String stage, String repo) {
            this.mtime = mtime;
            this.path = path;
            this.wdir = wdir;
            this.stage = stage;
            this.repo = repo;
        }
    }

    /**
     * Build gitlet repository if it doesn't exit.          <br>
     * The gitlet repository has the following structure:   <br>
     *                                                      <br>
     * CWD                                                  <br>
     * ├─── working tree                                    <br>
     * │    └── subtrees                                    <br>
     * │                                                    <br>
     * └─── .gitlet                                         <br>
     *      ├── objects                                     <br>
     *      │   ├── blobs                                   <br>
     *      │   └── commits                                 <br>
     *      ├── refs                                        <br>
     *      │   └── master                                  <br>
     *      ├── HEAD                                        <br>
     *      └── index                                       <br>
     */
    private void buildGitletRepository() {
        //Check whether gitlet repository already exists.
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            return;
        }

        try {
            //Make the .gitlet directory.
            var success = GITLET_DIR.mkdir();
            if (!success) {
                throw error("Failed to create gitlet repository");
            }

            //Object database system
            var objects = join(GITLET_DIR, "objects");
            var blobs = join(objects, "blobs");
            var commits = join(objects, "commits");
            if (!blobs.mkdirs() || !commits.mkdir()) {
                throw error("Failed to create gitlet object system");
            }

            //Create and store the first commit
            Commit initial = makeAndStoreCommit(null, null, "initial commit", ZonedDateTime.ofInstant(Instant.EPOCH, shanghai));

            //Create refs and track initial commit at master branch
            var refs = join(GITLET_DIR, "refs");
            var master = join(refs, "master");
            if (!refs.mkdir()) {
                throw error("Failed to create refs");
            }
            Files.write(master.toPath(), initial.getSha1().getBytes());

            //Move HEAD pointer
            moveHeadTo("master");
        } catch (GitletException | IOException e) {
            sanitize(GITLET_DIR);
            message(e.getMessage());
        }
    }

    /** Checking utils */

    /**
     * @return whether there is any file to commit.
     */
    boolean checkForFilesToCommit() {
        return false;
    }

    /**
     * @return whether the specified file is contained in the specified commit.
     */
    boolean checkForFileInCommit() {
        return false;
    }

    /**
     * @return whether the specified branch exists.
     */
    boolean checkBranch() {
        return false;
    }

    /**
     * @return whether the given commit id matches a commit object.
     */
    boolean checkCommit() {
        return false;
    }

    /**
     * @return whether there is no any untracked file vulnerable to RESET command.
     */
    boolean checkIfSafeToReset() {
        return false;
    }

    /* Reference utils*/

    /**
     * Move the HEAD pointer to the specified branch.
     */
    void moveHeadTo(String branch) {
        List<String> branches = Utils.plainFilenamesIn(join(GITLET_DIR, "refs"));
        if (!branches.contains(branch)) {
            throw error("no such branch: %s", branch);
        }

        var head = join(GITLET_DIR, "HEAD");
        try {
            Files.write(head.toPath(), branch.getBytes());
        } catch (IOException e) {
            throw error("Failed to move HEAD pointer, caused by %s", e.getMessage());
        }
    }

    /**
     * Delete the target file if it is a plain file, otherwise delete the directory and contained files.
     */
    void sanitize(File target) {
        if (target == null || !target.exists()) {
            return;
        }
        if (target.isDirectory()) {
            File[] files = target.listFiles();
            for (File file : files) {
                sanitize(file);
            }
        }
        target.delete();
    }

    Commit makeAndStoreCommit(HashMap<String, String> blobs, List<String> parents, String message, ZonedDateTime date) {
        Commit commit = new Commit(blobs, parents, message, Utils.format(date));
        var sha1 = commit.getSha1();
        var commitFile = join(GITLET_DIR, "objects", "commits", sha1);
        if (!commitFile.exists()) {
            Utils.writeObject(commitFile, commit);
        }
        return commit;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
//git status && template
//        String template = "=== Branches ===" + "%n" +
//                "%s" + "%n";
//        String branches = String.format("*%s%n%s", "master", "other-branch");
//        System.out.printf(template, branches);

//relative path
//        System.out.println(CWD);
//        File file = join(CWD, "test", "deep");
//        System.out.println(CWD.toPath().relativize(file.toPath()));

        getInstance().buildGitletRepository();

        var commits = join(GITLET_DIR, "objects", "commits");
        for (String commit: Utils.plainFilenamesIn(commits)) {
            var commitObject = Utils.readObject(join(commits, commit), Commit.class);
            commitObject.dump();
            System.out.println("---");
        }
    }
}
