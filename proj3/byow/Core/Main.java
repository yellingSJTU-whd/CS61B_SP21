package byow.Core;

import byow.TileEngine.TETile;

/** This is the main entry point for the program. This class simply parses
 *  the command line inputs, and lets the byow.Core.Game class take over
 *  in either keyboard or input string mode.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 1) {
            System.out.println("Can only have one argument - the input string");
            System.exit(0);
        } else if (args.length == 1) {
            Engine engine = new Engine();
            TETile[][] worldState = engine.playWithInputString(args[0]);
            System.out.println(TETile.toString(worldState));
        } else {
            Engine engine = new Engine();
            engine.playWithKeyboard();
        }
    }
}
