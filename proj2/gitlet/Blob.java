package gitlet;

import java.util.Objects;

import static gitlet.Repository.CWD;
import static gitlet.Utils.readContentsAsString;
import static gitlet.Utils.join;
import static gitlet.Utils.sha1;

/**
 * @author eYyoz
 * Immutable class to represent a file, judging equality by file content.
 */
public class Blob implements Dumpable {

    /**
     * Relative path under current working directory.
     */
    private final String filename;

    /**
     * Content of the underlying file.
     */
    private final String content;

    /**
     * SHA-1 HASH of this blob, derived from content.
     */
    private final String sha1;

    /**
     * @param  relativePath  relative file path under CWD
     * Constructor for blob. The existence of the file is unchecked.
     */
    Blob(String relativePath) {
        filename = relativePath;
        content = readContentsAsString(join(CWD, relativePath));
        sha1 = sha1(content);
    }

    /**
     * @return content of the underlying file
     */
    public String getContent() {
        return content;
    }

    /**
     * @return SHA1 hash of the blob
     */
    public String getSha1() {
        return sha1;
    }

    /**
     * @return relative path of the file as String
     */
    public String getFilename() {
        return filename;
    }

    @Override
    public void dump() {
        String info = "This is a Blob" + "%n"
                + "Relative path : " + filename + "%n"
                + content + "%n";
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
