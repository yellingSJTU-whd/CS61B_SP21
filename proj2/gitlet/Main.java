package gitlet;

import java.util.Objects;

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
                checkLength(len, 1);
                GitletService.init();
            }
            case "add" -> {
                checkLength(len, 2);
                GitletService.getInstance().add(args[1]);
            }
            case "commit" -> {
                if (len == 1 || args[1].isBlank()) {
                    exitWithMsg("Please enter a commit message.");
                }
                checkLength(len, 2);
                GitletService.getInstance().commit(args[1]);
            }
            case "rm" -> {
                checkLength(len, 2);
                GitletService.getInstance().rm(args[1]);
            }
            case "log" -> {
                checkLength(len, 1);
                GitletService.log();
            }
            case "global-log" -> {
                checkLength(len, 1);
                GitletService.globalLog();
            }
            case "find" -> {
                checkLength(len, 2);
                GitletService.find(args[1]);
            }
            case "status" -> {
                checkLength(len, 2);
                GitletService.getInstance().status();
            }
            case "checkout" -> {
                checkLength(len, 2, 4);
                GitletService.getInstance().checkout(args);
            }
            case "branch" -> {
                checkLength(len, 2);
                GitletService.branch(args[1]);
            }
            case "rm-branch" -> {
                checkLength(len, 2);
                GitletService.rmBranch(args[1]);
            }
            case "reset" -> {
                checkLength(len, 2);
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

    private static void checkLength(int length, int expected ) {
        if (length != expected) {
            exitWithMsg("Incorrect operands");
        }
    }

    private static void checkLength(int length, int min, int max) {
        if (length < min || length > max) {
            exitWithMsg("Incorrect operands");
        }
    }
}
