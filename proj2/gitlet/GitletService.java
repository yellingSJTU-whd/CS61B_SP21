package gitlet;

import java.util.Objects;

import static gitlet.Repository.GITLET_DIR;

/**
 * An abstraction over gitlet's functionality.
 *
 * @author eYyoz
 */
public class GitletService {

    private final Repository repository;
    private static GitletService service;

    private GitletService() {
        repository = Repository.getInstance();
    }

    /**
     * @return an instance of GitletService
     */
    public static GitletService getInstance() {
        if (service == null) {
            service = new GitletService();
        }
        return service;
    }

    public static void init() {
        try {
            if (!Repository.buildGitletRepository()) {
                Repository.sanitize(GITLET_DIR);
            }
        } catch (GitletException e) {
            Utils.message(e.getMessage());
            System.exit(0);
        }
    }

    public void add(String path) {
        try {
            if (repository.makeBlob(path)) {
                repository.saveIndex();
            }
        } catch (GitletException e) {
            Utils.message(e.getMessage());
            System.exit(0);
        }
    }

    public void commit(String message) {
        try {
            if (repository.makeCommit(message)) {
                repository.saveIndex();
            }
        } catch (GitletException e) {
            Utils.message(e.getMessage());
            System.exit(0);
        }
    }

    public void rm(String fileName) {
        try {
            repository.removeFromIndex(fileName);
            repository.saveIndex();
        } catch (GitletException e) {
            Utils.message(e.getMessage());
            System.exit(0);
        }
    }

    public static void log() {
        try {
            Repository.printLog();
        } catch (GitletException e) {
            Utils.message(e.getMessage());
            System.exit(0);
        }
    }

    public static void globalLog() {
        try {
            Repository.printGlobalLog();
        } catch (GitletException e) {
            Utils.message(e.getMessage());
            System.exit(0);
        }
    }

    public static void find(String message) {
        try {
            Repository.printCommitsByMessage(message);
        } catch (GitletException e) {
            Utils.message(e.getMessage());
            System.exit(0);
        }
    }

    public void status() {
        try {
            repository.printStatus();
        } catch (GitletException e) {
            Utils.message(e.getMessage());
            System.exit(0);
        }
    }

    public void checkout(String... args) {
        var len = args.length;
        switch (len) {
            case 2 -> {
                repository.checkoutBranch(args[1]);
            }
            case 3 -> {
                repository.checkoutFile(args[2]);
            }
            case 4 -> {
                repository.checkoutFile(args[1], args[3]);
            }
        }
    }

    public boolean branch() {
        return false;
    }

    public boolean rmBranch() {
        return false;
    }

    public boolean reset() {
        return false;
    }

    public boolean merge() {
        return false;
    }
}
