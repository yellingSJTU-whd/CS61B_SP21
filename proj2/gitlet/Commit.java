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

/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Commit implements Dumpable {
    /**
     * Blobs included in this commit.
     */
    private final List<Blob> blobs;

    /**
     * Sha1 hash for this commit, used as UID.
     */
    public final String sha1;

    /**
     * Parent commit(s), might be 0, 1 or 2.
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

    Commit(List<Blob> blobs, List<String> parents, String message, ZonedDateTime date) {
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
        builder.append("Parent(s):").append("%n");
        parents.forEach(parent -> builder.append(tab).append(parent).append("%n"));
        builder.append("Including blobs:").append("%n");
        blobs.forEach(blob -> builder.append(tab).append(Utils.join(CWD, blob.path).getName()).append(tab).append(blob.sha1));
        System.out.println(builder);
    }

    /* TODO: fill in the rest of this class. */

}
