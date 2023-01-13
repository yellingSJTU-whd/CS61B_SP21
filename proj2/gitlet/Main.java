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
        var len = args.length;
        if (len == 0) {
            Utils.message("Please enter a command.");
            System.exit(0);
        }

        var firstArg = args[0];
        var errMsg = "Incorrect operands";
        switch (firstArg) {
            case "init" -> {
                checkLength(args, 1);
                GitletService.getInstance().init();
            }
            case "add" -> {
                checkLength(args, 2);
                GitletService.getInstance().add(args[1]);
            }
            case "commit" -> {
                if (len == 1 || args[1].isBlank()) {
                    exitWithMsg("Please enter a commit message.");
                }
                checkLength(args, 2);
                GitletService.getInstance().commit(args[1]);

            }
            case "rm" -> {
                checkLength(args, 2);
                GitletService.getInstance().rm(args[1]);
            }
            case "log" -> {
                checkLength(args, 1);
                GitletService.getInstance().log();
            }
            case "global-log" -> {
                checkLength(args, 1);
                GitletService.getInstance().globalLog();
            }
            case "find" -> {
                checkLength(args, 2);
                GitletService.getInstance().find(args[1]);
            }
            // TODO: FILL THE REST IN
            default -> {
                exitWithMsg("No command with that name exists.");
            }
        }
    }

    private static void exitWithMsg(String msg) {
        Utils.message(msg);
        System.exit(0);
    }

    private static void checkLength(String[] args, int length) {
        if (args.length != length) {
            exitWithMsg("Incorrect operands");
        }
    }
}
