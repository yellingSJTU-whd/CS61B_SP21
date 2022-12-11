package gitlet;

import java.util.Objects;

import static gitlet.Repository.CWD;
import static gitlet.Utils.readContentsAsString;
import static gitlet.Utils.join;
import static gitlet.Utils.sha1;

/**
 * Immutable class to represent a file, judging equality by file content.
 */
public class Blob implements Dumpable {

    /**
     * Relative path under current working directory.
     */
    final String path;

    /**
     * Content of the underlying file.
     */
    private final String content;

    /**
     * Sha1 hash of this blob, derived from content.
     */
    public final String sha1;

    Blob(String relativePath) {
        path = relativePath;
        content = readContentsAsString(join(CWD, relativePath));
        sha1 = sha1(content);
    }

    @Override
    public void dump() {
        String info = "This is a Blob" + "%n" +
                "Relative path : " + path + "%n" +
                content + "%n";
        System.out.println(info);
    }

    @Override
    public int hashCode() {
        return Integer.decode(sha1);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Blob) {
            Blob that = (Blob) o;
            return Objects.equals(sha1, that.sha1);
        }
        return false;
    }
}
