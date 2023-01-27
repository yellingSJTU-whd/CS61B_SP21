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
    static GitletService getInstance() {
        if (service == null) {
            service = new GitletService();
        }
        return service;
    }

    static void init() {
        try {
            if (!Repository.buildGitletRepository()) {
                Utils.sanitize(GITLET_DIR);
            }
        } catch (GitletException e) {
            Utils.message(e.getMessage());
            System.exit(0);
        }
    }

    void add(String path) {
        try {
            if (repository.stageFile(path)) {
                repository.saveIndex();
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    void commit(String message) {
        try {
            repository.makeCommit(message);
            repository.saveIndex();
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    void rm(String fileName) {
        try {
            repository.removeFromIndex(fileName);
            repository.saveIndex();
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    static void log() {
        try {
            Repository.printLog();
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    static void globalLog() {
        try {
            Repository.printGlobalLog();
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    static void find(String message) {
        try {
            Repository.printCommitsByMessage(message);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    void status() {
        try {
            repository.printStatus();
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    void checkout(String... args) {
        var len = args.length;
        var modified = false;
        try {
            switch (len) {
                case 2 -> {
                    modified = repository.checkoutBranch(args[1]);
                }
                case 3 -> {
                    checkOperands(args[1]);
                    modified = repository.checkoutFile(args[2]);
                }
                case 4 -> {
                    checkOperands(args[2]);
                    modified = repository.checkoutFile(args[1], args[3]);
                }
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        if (modified) {
            repository.saveIndex();
        }
    }

    static void branch(String branch) {
        try {
            Repository.createBranch(branch);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    static void rmBranch(String branch) {
        try {
            Repository.removeBranch(branch);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    void reset(String sha1) {
        try {
            if (repository.reset(sha1)) {
                repository.saveIndex();
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    void merge(String branch) {
        try {
            if (repository.merge(branch)) {
                repository.saveIndex();
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    private static void checkOperands(String str) {
        if (!Objects.equals(str, "--")) {
            throw new GitletException("Incorrect operands");
        }
    }
}
