package gitlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a gitlet commit object.
 * @author eYyoz
 */
public class Commit implements Dumpable {
    /**
     * Blobs included in this commit.
     * Key:   file name
     * Value: SHA-1 HASH of the blob
     */
    private final Map<String, String> blobs;

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

    Commit(Map<String, String> blobs, List<String> parents, String message, String date) {
        this.blobs = blobs;
        this.parents = parents;
        this.message = message;
        this.date = date;

        var content = new ArrayList<>();
        if (blobs != null) {
            blobs.forEach((key, value) -> {
                content.add(key);
                content.add(value);
            });
        }
        if (parents != null) {
            content.addAll(parents);
        }
        if (message != null) {
            content.add(message);
        }
        if (date != null) {
            content.add(date);
        }
        sha1 = content.isEmpty() ? null : Utils.sha1(content);
    }

    @Override
    public void dump() {
        var tab = "    ";
        var builder = new StringBuilder();

        builder.append("This is a Commit").append("\n");
        builder.append("Date: ").append(date).append("\n");
        builder.append("Commit message: ").append(message).append("\n");

        builder.append(parents == null ? 0 : parents.size())
                .append(" parent(s)")
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

        System.out.println(builder);
    }

    public String getSha1() {
        return sha1;
    }

    public List<String> getParents() {
        return parents;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getBlobs() {
        if (blobs == null) {
            return new HashMap<>(0);
        }
        return blobs;
    }
}
