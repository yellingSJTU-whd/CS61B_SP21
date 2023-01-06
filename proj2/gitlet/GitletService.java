package gitlet;

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

    public boolean init() {
        return false;
    }

    public boolean add() {
        return false;
    }

    public boolean commit() {
        return false;
    }

    public boolean rm() {
        return false;
    }

    public void log() {

    }

    public void globalLog() {

    }

    public boolean find() {
        return false;
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
