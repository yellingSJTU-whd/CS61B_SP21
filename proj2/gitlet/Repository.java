package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author eYyoz
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

    /**
     * Object system.
     */
    public static final File OBJECTS = join(GITLET_DIR, "objects");

    /**
     * Blobs folder.
     */
    public static final File BLOBS = join(OBJECTS, "blobs");

    /**
     * Commits folder
     */
    public static final File COMMITS = join(OBJECTS, "commits");

    /**
     * References folder.
     */
    public static final File REFS = join(GITLET_DIR, "refs");

    /**
     * Index file.
     */
    public static final File INDEX = join(GITLET_DIR, "index");

    /**
     * HEAD pointer.
     */
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    private static String template;

    private static String mergeTemplate;

    /**
     * Similar to index file at real git, it tracks files and theirs status.
     * Key:    relative file path as String
     * Value:  file status represented as an entry instance
     */
    private final HashMap<String, Entry> index;

    private static Repository repository;

    @SuppressWarnings("unchecked")
    private Repository() {
        if (INDEX.exists() && INDEX.isFile()) {
            index = readObject(INDEX, HashMap.class);
        } else {
            index = new HashMap<>();
        }
    }

    /**
     * @return the singleton of Repository
     */
    public static Repository getInstance() {
        if (repository == null) {
            repository = new Repository();
        }
        return repository;
    }

    /* TODO: fill in the rest of this class. */

    /**
     * Shanghai zone id.
     */
    public static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    /**
     * Localized epoch for shanghai.
     */
    public static final ZonedDateTime EPOCH =
            ZonedDateTime.ofInstant(Instant.EPOCH, SHANGHAI);

    /**
     * SHA1 HASH mark for removal
     */
    public static final String removal = "0".repeat(UID_LENGTH);

    /**
     * An entry represents a file, tracking its status by keeping sha1 under three trees.
     */
    private static class Entry {

        /**
         * Modification time.
         */
        String mtime;

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

        Entry(String mtime, String path, String wdir, String stage, String repo) {
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
    void buildGitletRepository() {
        //Check whether gitlet repository already exists.
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            return;
        }

        //Make the .gitlet directory.
        var success = GITLET_DIR.mkdir();
        if (!success) {
            throw error("Failed to create .gitlet folder");
        }

        //Object database system
        if (!BLOBS.mkdirs() || !COMMITS.mkdir()) {
            throw error("Failed to create gitlet object system");
        }

        //Create and store the first commit
        var initial = makeAndStoreCommit(null, null, "initial commit", EPOCH);

        //Create refs and track initial commit at master branch
        var master = join(REFS, "master");
        if (!REFS.mkdir()) {
            throw error("Failed to create refs");
        }
        writeContents(master, initial.get().getSha1());

        //Move HEAD pointer
        moveHeadTo("master");
    }

    /**
     * Create and store a blob object, representing a file corresponding to given path,
     * if and only if the file exists and hasn't been saved.
     *
     * @param  path  relative file path under CWD
     * @return whether a new blob object is created and stored
     */
    boolean makeBlob(String path) {
        //check for initialized gitlet repository
        checkInitialization();

        //check whether the file correspond to the given path exists.
        var file = join(CWD, path);
        if (!file.exists()) {
            throw error("File does not exist.");
        }

        //create and store a blob object if not blob with same SHA-1 HASH has been saved.
        var blob = makeAndStoreBlob(path);
        blob.ifPresent(this::updateIndex);
        return blob.isPresent();
    }

    /**
     * Create and stored a commit object if there are files staged.
     *
     * @param  message  commit message
     * @return whether a new commit object is created and stored.
     */
    boolean makeCommit(String message) {
        //check for initialized gitlet repository
        checkInitialization();

        //check for staged files to commit, also update the index if possible
        var map = fetchStagedBlobsAndUpdateIndex();
        if (map.isEmpty()) {
            throw error("No changes added to the commit.");
        }

        //create and store a commit object
        var branch = fetchCurrentBranch();
        var lastCommit = fetchTipOfBranch(branch);
        var commit = makeAndStoreCommit(map.get(), List.of(lastCommit), message, ZonedDateTime.now(SHANGHAI));

        //moves the feature ref
        commit.ifPresent(c -> moveBranchTo(branch, c.getSha1()));
        return commit.isPresent();
    }

    /**
     * Remove a file from Gitlet if staged or tracked. If staged,
     * unstage it. If tracked, marked for removal and delete from
     * working tree if not already done. Throw exception otherwise.
     */
    void removeFromIndex(String fileName) {
        //check for initialized gitlet repository
        checkInitialization();

        //check whether the file is staged or tracked by tip of a branch
        if (!index.containsKey(fileName)) {
            throw error("No reason to remove the file.");
        }

        var entry = index.get(fileName);
        var stage = entry.stage;
        var repo = entry.repo;
        var file = join(CWD, entry.path);

        if (!Objects.equals(stage, repo)) {
            index.remove(fileName);
        } else {
            //stage for removal, the file will remove from index at next commit
            entry.stage = removal;
            file.delete();
        }
    }

    /**
     * Print log for current branch. Pick first parent when there are two.
     */
    static void printLog() {
        var builder = new StringBuilder();
        var sha1 = fetchTipOfBranch(fetchCurrentBranch());
        var curr = fetchCommit(sha1);

        while (curr != null) {
            var parents = curr.getParents();
            var isInitial = parents == null;

            appendCommit(curr, builder);
            curr = isInitial ? null : fetchCommit(parents.get(0));
        }
        message(builder.toString());
    }

    static void printGlobalLog() {
        var builder = new StringBuilder();
        var commits = plainFilenamesIn(COMMITS);

        if (commits == null) {
            System.out.println();
            return;
        }

        commits.forEach(sha1 -> {
            var commit = fetchCommit(sha1);
            appendCommit(commit, builder);
        });
        message(builder.toString());
    }

    static void printCommitsByMessage(String message) {
        var builder = new StringBuilder();
        var commits = plainFilenamesIn(COMMITS);
        if (commits == null) {
            System.out.println();
            return;
        }

        commits.forEach(sha1 -> {
            var commit = fetchCommit(sha1);
            var ls = System.lineSeparator();

            if (Objects.equals(commit.getMessage(), message)) {
                builder.append(sha1).append(ls);
            }
        });
        message(builder.toString());
    }

    void printStatus() {
        var template = buildStatusTemplate();
        var branches = fetchBranches();
        if (index.isEmpty()) {
            message(template, branches, "", "", "", "");
            return;
        }

//        var staged = new StringBuilder();
//        var removed = new StringBuilder();
//        var modifiedNotStaged = new StringBuilder();
//        var untracked = new StringBuilder();
//        message(template, branches, staged.toString(), removed.toString(), modifiedNotStaged.toString(), untracked.toString());

        var staged = new ArrayList<String>();
        var removed = new ArrayList<String>();
        var modifiedNotStaged = new ArrayList<String>();
        var untracked = plainFilenamesIn(CWD);
        index.values().forEach(entry -> {
                }
        );
    }

    /* Checking utils */

    /**
     * Check if there is a .gitlet folder.
     */
    private void checkInitialization() {
        if (!GITLET_DIR.exists()) {
            throw error("Not in an initialized Gitlet directory.");
        }
    }

    /**
     * @return whether the specified file is contained in the specified commit.
     */
    boolean checkForFileInCommit() {
        return false;
    }

    /**
     * @return whether the branch with given name exists.
     */
    boolean checkBranch(String branchName) {
        var branchFile = join(REFS, branchName);
        return branchFile.exists();
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
     * Move the HEAD pointer to the branch with given name if exists.
     */
    private static void moveHeadTo(String branchName) {
        var branchFile = join(REFS, branchName);
        if (!branchFile.exists()) {
            return;
        }

        try {
            Files.write(HEAD.toPath(), branchName.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private static String fetchBranches() {
        var list = plainFilenamesIn(REFS);
        var ls = System.lineSeparator();
        if (list == null) {
            return ls;
        }
        var builder = new StringBuilder();
        var curr = fetchCurrentBranch();
        list.forEach(branch -> {
            builder.append(branch).append(ls);
        });
        return builder.toString().replaceFirst(curr, "*" + curr);
    }

    /**
     * @return Name of the current branch.
     */
    private static String fetchCurrentBranch() {
        return readContentsAsString(HEAD);
    }

    /**
     * @param  branch  the target branch. Its existence is unchecked.
     * @return SHA-1 HASH of the commit at the tip of given branch
     */
    private static String fetchTipOfBranch(String branch) {
        var tip = join(REFS, branch);
        return readContentsAsString(tip);
    }

    private static void moveBranchTo(String branchName, String commitSha1) {
        var branchFile = join(REFS, branchName);
        writeContents(branchFile, commitSha1);
    }

    /* Object system utils*/

    /**
     * Fetch staged blobs, which wait to be committed, from index.
     * Also update if there is any files staged.
     */
    private Optional<HashMap<String, String>> fetchStagedBlobsAndUpdateIndex() {
        if (index.isEmpty()) {
            return Optional.empty();
        }

        var blobs = new HashMap<String, String>();
        index.values().forEach(entry -> {
            var sha1 = entry.stage;
            if (!Objects.equals(entry.repo, sha1)) {
                blobs.put(entry.path, sha1);

                if (!Objects.equals(sha1, removal)) {
                    index.remove(entry.path);
                } else {
                    entry.repo = sha1;
                }
            }
        });
        if (blobs.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(blobs);
    }

    private static Commit fetchCommit(String commitSha1) {
        return readObject(join(COMMITS, commitSha1), Commit.class);
    }


    /**
     * Delete the target file if it is a plain file, otherwise delete the directory and contained files.
     */
    void sanitize(File target) {
        if (target == null || !target.exists()) {
            return;
        }
        if (target.isDirectory()) {
            var optional = Optional.ofNullable(target.listFiles());
            optional.ifPresent(files -> {
                for (File file : files) {
                    sanitize(file);
                }
            });
        }
        target.delete();
    }

    /**
     * Save index if gitlet repository initialized and there are files been tracked.
     */
    void saveIndex() {
        if (GITLET_DIR.exists() && index != null) {
            writeObject(INDEX, index);
        }
    }

    Optional<Commit> makeAndStoreCommit(HashMap<String, String> blobs, List<String> parents, String message, ZonedDateTime date) {
        Commit commit = new Commit(blobs, parents, message, format(date));
        var commitFile = join(COMMITS, commit.getSha1());
        if (!commitFile.exists()) {
            writeObject(commitFile, commit);
            return Optional.of(commit);
        }
        return Optional.empty();
    }

    Optional<Blob> makeAndStoreBlob(String path) {
        var blob = new Blob(path);
        var blobFile = join(BLOBS, blob.getSha1());
        if (blobFile.exists()) {
            return Optional.empty();
        }
        writeObject(blobFile, blob);
        return Optional.of(blob);
    }

    /**
     * Update index with given blob.
     */
    void updateIndex(Blob blob){
        var path = blob.getPath();
        var time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(join(CWD, path).lastModified()), SHANGHAI);
        var entry = index.get(path);
        var sha1 = blob.getSha1();

        entry.mtime = format(time);
        entry.wdir = sha1;
        entry.stage = sha1;
    }

    HashMap<String, String> fetchAllBlobs() {
        var blobs = new HashMap<String, String>(index.size());
        for (Entry entry : index.values()) {
            String sha1 = entry.stage;
            entry.repo = sha1;
            entry.mtime = format(ZonedDateTime.now(SHANGHAI));
            blobs.put(entry.path, sha1);
        }
        return blobs;
    }


    /* Logging utils */

    private static void appendCommit(Commit commit, StringBuilder builder) {
        if (commit == null) {
            return;
        }

        var parents = commit.getParents();
        var isInitial = parents == null;

        if (isInitial || parents.size() < 2) {
            builder.append(String.format(fetchTemplate(),
                    commit.getSha1(), commit.getDate(), commit.getMessage()));
        } else {
            builder.append(String.format(fetchMergeTemplate(), commit.getSha1(),
                    parents.get(0).substring(0, 7),
                    parents.get(1).substring(0, 7),
                    commit.getDate(), commit.getMessage()));
        }
    }

    private static String fetchTemplate() {
        if (template == null) {
            template = buildTemplate();
        }
        return template;
    }

    private static String fetchMergeTemplate() {
        if (mergeTemplate == null) {
            mergeTemplate = buildMergeTemplate();
        }
        return mergeTemplate;
    }

    private static String buildTemplate() {
        var delimiter = "===";
        var ls = System.lineSeparator();

        return delimiter + ls
                + "commit %s" + ls
                + "Date: %s" + ls
                + "%s" + ls;
    }

    private static String buildMergeTemplate() {
        var delimiter = "===";
        var ls = System.lineSeparator();

        return delimiter + ls
                + "commit %s" + ls
                + "Merge: %s %s" + ls
                + "Date: %s" + ls
                + "%s" + ls;
    }

    private static String buildStatusTemplate() {
        var delimiter = "===";
        var ls = System.lineSeparator();

        return delimiter + " Branches " + delimiter + ls
                + "%s" + ls
                + delimiter + " Staged Files " + delimiter + ls
                + "%s" + ls
                + delimiter + " Removed Files " + delimiter + ls
                + "%s" + ls
                + delimiter + " Modifications Not Staged For Commit " + delimiter + ls
                + "%s" + ls
                + delimiter + " Untracked Files " + delimiter + ls
                + "%s";
    }

    public static void main(String[] args) {
//git status && template
//        String template = "=== Branches ===" + "%n" +
//                "%s" + "%n";
//        String branches = String.format("*%s%n%s", "master", "other-branch");
//        System.out.printf(template, branches);

//relative path
//        System.out.println(CWD);
//        File file = join(CWD, "test", "deep");
//        System.out.println(CWD.toPath().relativize(file.toPath()));

//try init
//        getInstance().buildGitletRepository();
//
//        var commits = join(GITLET_DIR, "objects", "commits");
//        for (String commit: Utils.plainFilenamesIn(commits)) {
//            var commitObject = Utils.readObject(join(commits, commit), Commit.class);
//            commitObject.dump();
//            System.out.println("---");
//        }

//long to ZonedDateTime
//        var file = join(CWD, "gitlet");
//        var time = file.lastModified();
//        System.out.println(time);
//        var converted = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), shanghai);
//        System.out.println(converted);
        getInstance().printStatus();
    }
}
