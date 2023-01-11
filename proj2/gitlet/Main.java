package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author eYyoz
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.message("Please enter a command.");
            System.exit(0);
        }

        var service = GitletService.getInstance();
        String firstArg = args[0];
        switch (firstArg) {
            case "init" -> service.init();
            case "add" -> {
                if (args.length != 2) {
                    service.exit("Incorrect operands.");
                }
                service.add(args[1]);
            }
            case "commit" -> {
                if (args.length!=2 || args[1].isBlank()){
                    Utils.message("Please enter a commit message.");
                } else {
                    service.commit(args[1]);
                }
            }
            // TODO: FILL THE REST IN
            default -> {
                Utils.message("No command with that name exists.");
                System.exit(0);
            }
        }
    }
}
