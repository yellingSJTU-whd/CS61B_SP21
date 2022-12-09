package gitlet;

import java.util.Objects;

import static gitlet.Repository.CWD;
import static gitlet.Utils.readContentsAsString;
import static gitlet.Utils.join;
import static gitlet.Utils.sha1;

public class Blob implements Dumpable {

    final String path;
    final String content;
    final String sha1;

    Blob(String relativePath) {
        path = relativePath;
        content = readContentsAsString(join(CWD, relativePath));
        sha1 = sha1(content);
    }

    @Override
    public void dump() {
        String info = "This is a blob" + "%n" +
                "Relative path: " + path + "%n" +
                "content: " + content + "%n";
        System.out.println(info);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sha1);
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
