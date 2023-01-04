package gitlet;

// TODO: any imports you need here

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.List;

import static gitlet.Repository.CWD;
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
     * Localized date when this commit created, or Unix epoch if no predecessor.
     */
    private final ZonedDateTime date;

    Commit(HashMap<String, String> blobs, List<String> parents, String message, ZonedDateTime date) {
        this.blobs = blobs;
        this.parents = parents;
        this.message = message;
        this.date = date;

        sha1 = Utils.sha1(blobs);
    }

    @Override
    public void dump() {
        var tab = "    ";

        var builder = new StringBuilder();
        builder.append("This is a Commit").append("%n");
        builder.append("Date: ").append(Utils.date2String(date));
        builder.append("Commit message: ").append(message);

        builder.append(parents.size()).append(" parent(s):").append("%n");
        parents.forEach(parent ->
                builder.append(tab).append(parent).append("%n"));

        builder.append("Including ")
                .append(blobs.size())
                .append(" blobs")
                .append("%n");
//        blobs.forEach(blob ->
//                builder.append(tab)
//                        .append(join(CWD, blob.path).getName())
//                        .append(tab)
//                        .append(blob.sha1));

        System.out.println(builder);
    }

    /* TODO: fill in the rest of this class. */

}
