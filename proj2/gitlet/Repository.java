package gitlet;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.Locale;

import static gitlet.Utils.*;

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

    private static final HashMap<String, Node> index;

    static {
        var indexFile = join(GITLET_DIR, "index");
        if (indexFile.exists() && indexFile.isFile()) {
            index = readObject(indexFile, HashMap.class);
        } else {
            try {
                indexFile.createNewFile();
            } catch (IOException e) {
                throw error("fatal error: gitlet directory corrupted");
            }
            index = new HashMap<>();
        }
    }

    /* TODO: fill in the rest of this class. */
    static final ZoneId shanghai = ZoneId.of("Asia/Shanghai");

    private static class Node {
        ZonedDateTime lastModifiedTime;
        String path;
        String wdir;
        String stage;
        String repo;

        Node(ZonedDateTime date, String path, String wdir, String stage, String repo) {

        }
    }

    public static void main(String[] args) {
//        var time = LocalDateTime.ofInstant(Instant.ofEpochSecond(0L), ZoneId.of("Asia/Shanghai"));
//        var formatter = DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm:ss");
//        System.out.println(time.format(formatter));

//        var date = ZonedDateTime.now(shanghai);
//        var formatter = DateTimeFormatter.ofPattern("EEE LLL d kk:mm:ss yyyy Z").localizedBy(Locale.ENGLISH);
//        System.out.println(date.format(formatter));

//        var file = join(GITLET_DIR, "index");
//        System.out.println(file.getPath());
//        System.out.println(file.getAbsolutePath());

//        var file = join(CWD, "index");
//        try {
//            file.createNewFile();
//            writeContents(file, "abc");
//            System.out.println(readContentsAsString(file));
//            System.out.println("====");
//            writeContents(file);
//            System.out.println(readContentsAsString(file));
//            System.out.println("====");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
}
