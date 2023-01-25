package gitlet;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
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

    /**
     * Shanghai zone id.
     */
    public static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    /**
     * Localized epoch for shanghai.
     */
    public static final ZonedDateTime EPOCH = Instant.EPOCH.atZone(SHANGHAI);

    /**
     * SHA1 HASH mark for REMOVAL
     */
    public static final String REMOVAL = "0".repeat(UID_LENGTH);

    /**
     * An entry represents a file, tracking its status by keeping sha1 under three trees.
     */
    private static class Entry {
        long mtime;

        String path;

        String wdir;

        String stage;

        String repo;

        Entry(long mtime, String path, String wdir, String stage, String repo) {
            this.mtime = mtime;
            this.path = path;
            this.wdir = wdir;
            this.stage = stage;
            this.repo = repo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Entry entry = (Entry) o;
            return path.equals(entry.path)
                    && mtime == entry.mtime
                    && Objects.equals(wdir, entry.wdir)
                    && Objects.equals(stage, entry.stage)
                    && Objects.equals(repo, entry.repo);
        }

        @Override
        public int hashCode() {
            int result = (int) (mtime ^ (mtime >>> 32));
            result = 31 * result + path.hashCode();
            result = 31 * result + (wdir != null ? wdir.hashCode() : 0);
            result = 31 * result + (stage != null ? stage.hashCode() : 0);
            result = 31 * result + (repo != null ? repo.hashCode() : 0);
            return result;
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
    static boolean buildGitletRepository() {
        if (GITLET_DIR.exists()) {
            throw error("A Gitlet version-control system already exists in the current directory.");
        }
        if (!GITLET_DIR.mkdir() || !BLOBS.mkdirs() || !COMMITS.mkdir() || !REFS.mkdir()) {
            return false;
        }

        var initial = new Commit(null, null,
                "initial commit", format(Instant.EPOCH.atZone(SHANGHAI)));

        storeCommit(initial);
        writeContents(join(REFS, "master"), initial.getSha1());
        moveHeadTo("master");
        return true;
    }

    /**
     * Create and store a blob object, representing a file corresponding to given path,
     * if and only if the file exists and hasn't been saved.
     */
    boolean stageFile(String filename) {
        checkInitialization();
        if (!join(CWD, filename).exists()) {
            throw error("File does not exist.");
        }

        var blob = new Blob(filename);
        storeBlob(blob);
        return updateIndex(blob);
    }

    /**
     * Create and stored a commit object if there are files staged.
     */
    void makeCommit(String message) {
        checkInitialization();
        var map = fetchStagedAndUpdateIndex();
        if (map.isEmpty()) {
            throw error("No changes added to the commit.");
        }

        var branch = fetchCurrentBranch();
        var lastCommit = fetchTipOfBranch(branch);
        var commit = new Commit(map, List.of(lastCommit),
                message, format(ZonedDateTime.now(SHANGHAI)));

        storeCommit(commit);
        moveBranchTo(branch, commit.getSha1());
    }

    /**
     * Remove a file from Gitlet if staged or tracked.
     * If staged, unstage it. If tracked, marked for REMOVAL and delete
     * from working tree if not already done. Throw exception otherwise.
     */
    void removeFromIndex(String fileName) {
        checkInitialization();
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
            //stage for REMOVAL, the file will remove from index at next commit
            entry.stage = REMOVAL;
            file.delete();
        }
    }

    /**
     * Print log for current branch. Pick first parent when there are two.
     */
    static void printLog() {
        checkInitialization();
        var builder = new StringBuilder();
        var curr = fetchCommit(fetchTipOfBranch(fetchCurrentBranch()));

        while (curr != null) {
            var parents = curr.getParents();
            var isInitial = parents == null;

            appendCommit(curr, builder);
            curr = isInitial ? null : fetchCommit(parents.get(0));
        }
        System.out.println(builder);
    }

    static void printGlobalLog() {
        checkInitialization();
        var builder = new StringBuilder();
        var dir = COMMITS.toPath();

        applyToPlainFilesIn(dir, commit -> {
            var sha1 = dir.relativize(commit).toString();
            appendCommit(fetchCommit(sha1), builder);
        });
        System.out.println(builder);
    }

    static void printCommitsByMessage(String message) {
        checkInitialization();
        var builder = new StringBuilder();
        var dir = COMMITS.toPath();

        applyToPlainFilesIn(dir, commit -> {
            var sha1 = dir.relativize(commit).toString();
            if (Objects.equals(fetchCommit(sha1).getMessage(), message)) {
                builder.append(sha1).append(System.lineSeparator());
            }
        });

        if (builder.length() == 0) {
            throw error("Found no commit with that message.");
        }
        System.out.println(builder);
    }

    void printStatus() {
        checkInitialization();
        if (index.isEmpty()) {
            System.out.printf(buildStatusTemplate(), fetchBranchesStatus(), "", "", "", "");
            return;
        }

        var staged = new ArrayList<String>();
        var removed = new ArrayList<String>();
        var modifiedNotStaged = new ArrayList<String>();
        var untracked = plainFilenamesIn(CWD);
        var deletedPostfix = " (deleted)";
        var modifiedPostfix = " (modified)";

        index.values()
            .forEach(entry -> {
                    var path = entry.path;
                    var file = join(CWD, path);

                    if (untracked != null) {
                        untracked.remove(path);
                    }
                    if (Objects.equals(REMOVAL, entry.stage)) {
                        removed.add(path);
                    } else if (!Objects.equals(entry.stage, entry.repo)) {
                        staged.add(path);
                    }
                    //check for difference between working tree and index file,
                    //including unstaged modification or removal.
                    if (!file.exists()) {
                        modifiedNotStaged.add(path + deletedPostfix);
                        entry.wdir = REMOVAL;
                    } else {
                        var lastModified = file.lastModified();
                        if (!Objects.equals(entry.mtime, lastModified)) {
                            modifiedNotStaged.add(path + modifiedPostfix);
                            entry.mtime = lastModified;
                            entry.wdir = sha1(readContentsAsString(file));
                        }
                    }
                }
            );

        System.out.printf(buildStatusTemplate(),
                fetchBranchesStatus(),
                reduce(staged),
                reduce(removed),
                reduce(modifiedNotStaged),
                untracked == null ? "" : reduce(untracked));
    }

    boolean checkoutFile(String filename) {
        checkInitialization();
        return checkoutFile(fetchTipOfBranch(fetchCurrentBranch()), filename);
    }

    boolean checkoutFile(String commitSha1, String filename) {
        checkInitialization();
        checkCommit(commitSha1);
        var blobs = fetchCommit(commitSha1).getBlobs();
        if (!blobs.containsKey(filename)) {
            throw error("File does not exist in that commit.");
        }

        //check difference between working tree and repo
        var blobSha1 = blobs.get(filename);
        var entry = index.get(filename);
        if (Objects.equals(entry.wdir, blobSha1)) {
            return false;
        }

        //overwrite the file in working tree
        var blob = fetchBlob(blobSha1);
        var content = blob.getContent();
        var file = join(CWD, filename);
        writeContents(file, content);

        //update index
        entry.wdir = sha1(content);
        entry.mtime = file.lastModified();
        return true;
    }

    /**
     * Check out tip of the given branch if possible. Throw GitletException at failure cases.
     */
    boolean checkoutBranch(String branch) {
        checkInitialization();
        if (!checkBranch(branch)) {
            throw error("No such branch exists.");
        }
        if (Objects.equals(fetchCurrentBranch(), branch)) {
            throw error("No need to checkout the current branch.");
        }

        var indexModified = checkoutCommit(fetchTipOfBranch(branch));
        moveHeadTo(branch);
        return indexModified;
    }

    boolean reset(String sha1) {
        checkInitialization();
        var indexModified = checkoutCommit(sha1);
        moveBranchTo(fetchCurrentBranch(), sha1);
        return indexModified;
    }

    static void createBranch(String branch) {
        checkInitialization();
        if (checkBranch(branch)) {
            throw error("A branch with that name already exists.");
        }
        var sha1 = fetchTipOfBranch(fetchCurrentBranch());
        writeContents(join(REFS, branch), sha1);
    }

    static void removeBranch(String branch) {
        checkInitialization();
        if (!checkBranch(branch)) {
            throw error("A branch with that name does not exist.");
        }
        if (Objects.equals(fetchCurrentBranch(), branch)) {
            throw error("Cannot remove the current branch.");
        }
        sanitize(join(REFS, branch));
    }

    boolean merge(String branch) {
        checkInitialization();
        if (!checkBranch(branch)) {
            throw error("A branch with that name does not exist.");
        }
        var currBranch = fetchCurrentBranch();
        if (Objects.equals(currBranch, branch)) {
            throw error("Cannot merge a branch with itself.");
        }
        index.values().forEach(entry -> {
            var stageSha1 = entry.stage;
            var repoSha1 = entry.repo;
            if (!Objects.equals(stageSha1, repoSha1)) {
                throw error("You have uncommitted changes.");
            }
        });

        var mergeSha1 = fetchTipOfBranch(branch);
        var headSha1 = fetchTipOfBranch(fetchCurrentBranch());
        var mergeCommit = fetchCommit(mergeSha1);
        var headCommit = fetchCommit(headSha1);

        if (checkAncestry(headCommit, mergeSha1)) {
            throw error("Given branch is an ancestor of the current branch.");
        }
        if (checkAncestry(mergeCommit, headSha1)) {
            checkoutCommit(mergeCommit);
            throw error("Current branch fast-forwarded.");
        }
        var identical = checkBeforeCheckout(mergeCommit);
        var msg = String.format("Merged %s into %s", currBranch, branch);
        return trueMerge(headCommit, mergeCommit, identical, msg);
    }

    /* Checking utils */

    /**
     * Check if there is a .gitlet folder.
     */
    private static void checkInitialization() {
        if (!GITLET_DIR.exists()) {
            throw error("Not in an initialized Gitlet directory.");
        }
    }

    /**
     * @return whether the branch with given name exists.
     */
    private static boolean checkBranch(String branchName) {
        var branchFile = join(REFS, branchName);
        return branchFile.exists() && branchFile.isFile();
    }

    /**
     * Check whether the given SHA1 HASH match a saved commit object.
     */
    private void checkCommit(String sha1) {
        if (!join(COMMITS, sha1.substring(0, 2), sha1.substring(2)).exists()) {
            throw error("No commit with that id exists.");
        }
    }

    private Set<String> checkBeforeCheckout(Commit commit) {
        checkCommit(commit.getSha1());
        var files = plainFilenamesIn(CWD);
        var blobs = commit.getBlobs();
        var identical = new HashSet<String>();
        if (files != null) {
            files.removeAll(index.keySet());
            files.retainAll(blobs.keySet());
            var iter = files.listIterator();
            while (iter.hasNext()) {
                var filename = iter.next();
                var repoSha1 = blobs.get(filename);
                var wdirSha1 = sha1(readContentsAsString(join(CWD, filename)));
                if (Objects.equals(repoSha1, wdirSha1)) {
                    identical.add(filename);
                } else {
                    iter.remove();
                }
            }
        }
        if (files != null && !files.isEmpty()) {
            throw error("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
        }
        return identical;
    }

    private static boolean checkAncestry(Commit descendant, String ancestor) {
        if (descendant == null) {
            return false;
        }
        if (Objects.equals(descendant.getSha1(), ancestor)) {
            return true;
        }
        var parents = descendant.getParents();
        if (parents == null) {
            return false;
        }

        for (String parent : descendant.getParents()) {
            if (checkAncestry(fetchCommit(parent), ancestor)) {
                return true;
            }
        }
        return false;
    }

    private void makeMergeCommit(String message, String mergeSha1) {
        var map = fetchStagedAndUpdateIndex();
        if (map.isEmpty()) {
            throw error("No changes added to the commit.");
        }
        var branch = fetchCurrentBranch();
        var commit = new Commit(map, List.of(fetchTipOfBranch(branch), mergeSha1),
                message, format(ZonedDateTime.now(SHANGHAI)));

        storeCommit(commit);
        moveBranchTo(branch, commit.getSha1());
    }

    /* Reference utils*/

    /**
     * Move the HEAD pointer to the branch with given name if exists.
     */
    private static void moveHeadTo(String branchName) {
        writeContents(HEAD, branchName);
    }

    private static String fetchBranchesStatus() {
        var dir = REFS.toPath();
        var builder = new StringBuilder();
        var curr = fetchCurrentBranch();

        applyToPlainFilesIn(dir, ref -> {
            var branch = dir.relativize(ref).toString();
            builder.append(branch).append(System.lineSeparator());
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
     * @return SHA-1 HASH of the commit at the tip of given branch
     */
    private static String fetchTipOfBranch(String branch) {
        return readContentsAsString(join(REFS, branch));
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
    private HashMap<String, String> fetchStagedAndUpdateIndex() {
        if (index.isEmpty()) {
            return new HashMap<>(0);
        }

        var blobs = new HashMap<String, String>();
        index.values().forEach(entry -> {
            var stage = entry.stage;
            if (!Objects.equals(entry.repo, stage)) {
                blobs.put(entry.path, stage);
                if (Objects.equals(stage, REMOVAL)) {
                    index.remove(entry.path);
                } else {
                    entry.repo = stage;
                }
            }
        });
        if (blobs.isEmpty()) {
            return new HashMap<>(0);
        }
        return blobs;
    }

    private boolean checkoutCommit(Commit commit) {
        var identical = checkBeforeCheckout(commit);
        var blobs = commit.getBlobs();

        //create or overwrite files and update index
        var modified = new AtomicReference<Boolean>(false);
        blobs.forEach((filename, sha1) -> {
            if (!identical.contains(filename)) {
                var file = join(CWD, filename);
                var blob = fetchBlob(sha1);
                writeContents(file, blob.getContent());
                modified.set(true);
                index.put(filename, new Entry(file.lastModified(), filename, sha1, sha1, sha1));
            }
        });

        //delete file and update index
        for (String fileName : index.keySet()) {
            if (!blobs.containsKey(fileName)) {
                modified.set(true);
                sanitize(join(CWD, fileName));
            }
        }
        index.keySet().retainAll(blobs.keySet());
        return modified.get();
    }

    private boolean checkoutCommit(String commitSha1) {
        return checkoutCommit(fetchCommit(commitSha1));
    }

    private static Commit fetchCommit(String sha1) {
        var dir = join(COMMITS, sha1.substring(0, 2));
        var files = plainFilenamesIn(dir);
        if (files == null) {
            throw new RuntimeException();
        }

        var remainder = sha1.substring(2);
        for (String filename : files) {
            if (filename.startsWith(remainder)) {
                return readObject(join(dir, filename), Commit.class);
            }
        }
        throw new RuntimeException();
    }

    private static Blob fetchBlob(String sha1) {
        var dir = join(BLOBS, sha1.substring(0, 2));
        var files = plainFilenamesIn(dir);
        if (files == null) {
            throw new RuntimeException();
        }
        var remainder = sha1.substring(2);
        for (String filename : files) {
            if (filename.startsWith(remainder)) {
                return readObject(join(dir, filename), Blob.class);
            }
        }
        throw new RuntimeException();
    }

    /**
     * Save index if gitlet repository initialized and there are files been tracked.
     */
    void saveIndex() {
        if (GITLET_DIR.exists() && index != null) {
            writeObject(INDEX, index);
        }
    }

    private static void storeBlob(Blob blob) {
        var sha1 = blob.getSha1();
        var dir = join(BLOBS, sha1.substring(0, 2));
        var file = join(dir, sha1.substring(2));
        if (file.exists()) {
            return;
        }
        if (!(dir.exists() || dir.mkdirs())) {
            throw new RuntimeException();
        }
        writeObject(file, blob);
    }

    private static void storeCommit(Commit commit) {
        var sha1 = commit.getSha1();
        var dir = join(COMMITS, sha1.substring(0, 2));
        var file = join(dir, sha1.substring(2));
        if (file.exists()) {
            return;
        }
        if (!(dir.exists() || dir.mkdirs())) {
            throw new RuntimeException();
        }
        writeObject(file, commit);
    }

    private boolean trueMerge(Commit head, Commit merge, Set<String> identical, String msg) {
        var headBlobs = head.getBlobs();
        var mergeBlobs = merge.getBlobs();
        var headSet = headBlobs.keySet();
        var mergeSet = mergeBlobs.keySet();
        var mergeOnly = new HashSet<String>(mergeSet);
        var diff = new HashSet<String>(headSet);
        mergeOnly.removeAll(headSet);
        diff.retainAll(mergeSet);
        diff.removeAll(identical);

        mergeOnly.forEach(filename -> {
            var blob = fetchBlob(mergeBlobs.get(filename));
            var file = join(CWD, filename);
            var sha1 = blob.getSha1();

            writeContents(file, blob.getContent());
            index.put(filename,
                    new Entry(file.lastModified(), filename, sha1, sha1, null));
        });
        diff.forEach(filename -> {
            var ls = System.lineSeparator();
            var content = "<<<<<<< HEAD" + ls
                    + fetchBlob(headBlobs.get(filename)).getContent()
                    + "=======" + ls
                    + fetchBlob(mergeBlobs.get(filename)).getContent()
                    + ">>>>>>>";
            var entry = index.get(filename);
            var file = join(CWD, filename);

            writeContents(file, content);
            entry.mtime = file.lastModified();
            var blob = new Blob(filename);
            storeBlob(blob);
            updateIndex(blob);
        });

        makeMergeCommit(msg, merge.getSha1());
        if (!diff.isEmpty()) {
            System.out.println("Encountered a merge conflict.");
        }
        return mergeOnly.isEmpty() && diff.isEmpty();
    }

    /**
     * Update index with given blob.
     */
    private boolean updateIndex(Blob blob) {
        var filename = blob.getFilename();
        var sha1 = blob.getSha1();
        var mtime = join(CWD, filename).lastModified();
        var entry = new Entry(mtime, filename, sha1, sha1, sha1);
        var modified = !Objects.equals(entry, index.put(filename, entry));
        if (modified) {
            entry.mtime = mtime;
            entry.wdir = sha1;
            entry.stage = sha1;
        }
        return modified;
    }


    /* Logging utils */

    /**
     * Append a commit object to the StringBuilder in predefined format.
     * An internal method in support of gitlet logging.
     */
    private static void appendCommit(Commit commit, StringBuilder builder) {
        if (commit == null) {
            return;
        }
        var parents = commit.getParents();
        if (parents == null || parents.size() < 2) {
            builder.append(String.format(fetchTemplate(),
                    commit.getSha1(), commit.getDate(), commit.getMessage()));
        } else {
            builder.append(String.format(fetchMergeTemplate(), commit.getSha1(),
                    parents.get(0).substring(0, 7),
                    parents.get(1).substring(0, 7),
                    commit.getDate(), commit.getMessage()));
        }
    }

    /**
     * Fetch template for printing a commit with at most one parent.
     */
    private static String fetchTemplate() {
        if (template == null) {
            template = buildTemplate();
        }
        return template;
    }

    /**
     * Fetch template for printing a commit with two parents.
     */
    private static String fetchMergeTemplate() {
        if (mergeTemplate == null) {
            mergeTemplate = buildMergeTemplate();
        }
        return mergeTemplate;
    }

    /**
     * Construct template for printing a commit with at most one parent.
     */
    private static String buildTemplate() {
        var delimiter = "===";
        var ls = System.lineSeparator();

        return delimiter + ls
                + "commit %s" + ls
                + "Date: %s" + ls
                + "%s" + ls;
    }

    /**
     * Construct template for printing a commit with two parents.
     */
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

    /**
     * Sort a list destructive and reduce to a new String, with each element in a row.
     */
    private static String reduce(List<String> list) {
        return list.stream()
                .sorted(String::compareTo)
                .reduce("", (str1, str2) -> str1 + str2 + System.lineSeparator());
    }
}
