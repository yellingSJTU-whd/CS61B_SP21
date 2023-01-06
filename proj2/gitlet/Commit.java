package gitlet;

// TODO: any imports you need here

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static gitlet.Utils.format;
import static gitlet.Utils.join;

/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author eYyoz
 */
public class Commit implements Dumpable {
    /**
     * Blobs included in this commit.
     * Key:   relative path under CWD
     * Value: SHA-1 HASH of the blob
     */
    private final HashMap<String, String> blobs;

    /**
     * Sha1 hash for this commit, used as UID.
     */
    private final String sha1;

    /**
     * Parent commit(s), there might be 0, 1 or 2.
     */
    private final List<String> parents;

    /**
     * The message of this Commit.
     */
    private final String message;

    /**
     * Localized date with predefined format when this commit created,
     * or Unix epoch if no predecessor.
     */
    private final String date;

    Commit(HashMap<String, String> blobs, List<String> parents, String message, String date) {
        this.blobs = blobs;
        this.parents = parents;
        this.message = message;
        this.date = date;

        List<Object> list = new ArrayList<>();
        if (blobs != null) {
            for (Map.Entry<String, String> entry : blobs.entrySet()) {
                list.add(entry.getKey());
                list.add(entry.getValue());
            }
        }
        if (parents != null) {
            list.addAll(parents);
        }
        list.add(message);
        list.add(this.date);
        sha1 = Utils.sha1(list);
    }

    @Override
    public void dump() {
        var tab = "    ";
        var builder = new StringBuilder();

        builder.append("This is a Commit").append("\n");
        builder.append("Date: ").append(date).append("\n");
        builder.append("Commit message: ").append(message).append("\n");

        builder.append(parents == null ? 0 : parents.size())
                .append(" parent(s):")
                .append("\n");
        Optional.ofNullable(parents)
                .ifPresent(commits -> commits.forEach
                        (commit -> builder.append(tab).append(commit).append("\n")));

        builder.append("Including ")
                .append(blobs == null ? 0 : blobs.size())
                .append(" blobs")
                .append("\n");
        Optional.ofNullable(blobs)
                .ifPresent(files -> files.keySet().forEach
                        (file -> builder.append(tab).append(file).append("\n")));
    }

    /* TODO: fill in the rest of this class. */

    public String getSha1() {
        return sha1;
    }
}
