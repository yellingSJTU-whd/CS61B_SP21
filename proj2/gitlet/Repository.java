package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;

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
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

//    private static final HashMap<String, Node> index;
//
//    /**
//     *  Load index file if exists.
//     */
//    static {
//        var indexFile = join(GITLET_DIR, "index");
//        if (indexFile.exists() && indexFile.isFile()) {
//            index = readObject(indexFile, HashMap.class);
//        }
//    }

    /* TODO: fill in the rest of this class. */
    public static final ZoneId shanghai = ZoneId.of("Asia/Shanghai");

    /**
     * An entry represents a file, tracking its status by keeping sha1 under three trees.
     */
    private static class Entry {

        /**
         * Last time when the content of a file is modified, rather than metadata.
         */
        ZonedDateTime lastModifiedTime;

        /**
         * Relative path under Current Working Directory.
         */
        String path;

        /**
         * Sha1 of the file content under Current Working Directory.
         */
        String workingTree;

        /**
         * Sha1 of
         */
        String stagingTree;
        String commitTree;

        Entry(ZonedDateTime date, String path, String workingTree, String stagingTree, String commitTree) {

        }
    }

    /**
     * Build gitlet repository if it doesn't exit.          <br>
     * The gitlet repository has the following structure:   <br>
     * CWD                                                  <br>
     * ├─── working tree                                    <br>
     * │    └── subtree                                     <br>
     * │                                                    <br>
     * └── .gitlet                                          <br>
     * ├── objects                                      <br>
     * │   └── blobs                                    <br>
     * │   └── commits                                  <br>
     * ├── refs                                         <br>
     * │   └── heads                                    <br>
     * ├── HEAD                                         <br>
     * └── index                                        <br>
     */
    private static void buildGitletRepository() {

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        String template = "=== Branches ===" + "%n" +
                "%s" + "%n";
        String branches = String.format("*%s%n%s", "master", "other-branch");
        System.out.printf(template, branches);
    }
}
