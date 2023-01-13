package gitlet;

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

    public void init() {
        try {
            repository.buildGitletRepository();
        } catch (GitletException e) {
            repository.sanitize(GITLET_DIR);
            System.exit(0);
        }
    }

    public void add(String path) {
        try {
            if (repository.makeBlob(path)) {
                repository.saveIndex();
            }
        } catch (GitletException e){
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

    public void log() {
        Repository.printLog();
    }

    public void globalLog() {
        Repository.printGlobalLog();
    }

    public void find(String message) {
        Repository.printCommitsByMessage(message);
    }

    public void status() {

    }

    public boolean checkout() {
        return false;
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
