package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 81;
    public static final int HEIGHT = 31;
    private static final Path save = Paths.get("byow", "Core", "save.txt");
    private StringBuilder operations;
    private Player player;
    private Random random;
    private TETile[][] theWorld;
    private List<Position> portals;



    private void newGame() {
        operations = new StringBuilder();
        operations.append("N");

        promptToSeedUi();
        String seed = solicitSeed();
        operations.append(seed).append("S");
        saveOperations();

        theWorld = generateWorld(Long.parseLong(seed));
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 14));
        ter.renderFrameWithShadow(theWorld, player.getPosition(), 15);
        StdDraw.setPenColor(Color.ORANGE);
        StdDraw.line(0, HEIGHT, WIDTH, HEIGHT);
        StdDraw.show();

        interact();
    }

    private void loadGame() {
        operations = new StringBuilder(loadOperations());
        if (operations.length() == 0) {
            System.exit(0);
        }
        theWorld = interactWithInputString(String.valueOf(operations));
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 14));
        ter.renderFrameWithShadow(theWorld, player.getPosition(), 15);
    }

    private void renderHUD(String gameInfo) {
        var x = (int) StdDraw.mouseX();
        var y = (int) StdDraw.mouseY();
        var description = "";

        if (!new Position(x, y).outOf(theWorld)) {
            description = theWorld[x][y].description();
        }
        StdDraw.setFont(new Font("Monaco", Font.PLAIN, 25));
        StdDraw.setPenColor(Color.ORANGE);
        StdDraw.textLeft(0, HEIGHT + 1, description);
        StdDraw.text(WIDTH / 2.0, HEIGHT + 1, gameInfo);
        StdDraw.line(0, HEIGHT, WIDTH, HEIGHT);

        StdDraw.show();
    }

    private void repaintWall(Direction direction) {
        var currX = player.getPosition().getX();
        var currY = player.getPosition().getY();
        TETile wall;
        switch (direction) {
            case UP -> {
                if (theWorld[currX][currY + 1].equals(Tileset.WALL)) {
                    wall = theWorld[currX][currY + 1];
                    theWorld[currX][currY + 1] = TETile.colorVariant(wall, 60, 60, 60, random);
                }
            }
            case DOWN -> {
                if (theWorld[currX][currY - 1].equals(Tileset.WALL)) {
                    wall = theWorld[currX][currY - 1];
                    theWorld[currX][currY - 1] = TETile.colorVariant(wall, 60, 60, 60, random);
                }
            }
            case LEFT -> {
                if (theWorld[currX - 1][currY].equals(Tileset.WALL)) {
                    wall = theWorld[currX - 1][currY];
                    theWorld[currX - 1][currY] = TETile.colorVariant(wall, 60, 60, 60, random);
                }
            }
            case RIGHT -> {
                if (theWorld[currX + 1][currY].equals(Tileset.WALL)) {
                    wall = theWorld[currX + 1][currY];
                    theWorld[currX + 1][currY] = TETile.colorVariant(wall, 60, 60, 60, random);
                }
            }
            default -> {
            }
        }
    }

    private void interact() {
        while (true) {
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
            char input = Character.toUpperCase(StdDraw.nextKeyTyped());
            switch (input) {
                case 'W' -> {
                    if (player.moveNorth(theWorld)) {
                        operations.append("W");
                        reDraw("went north");
                        handleTeleport();
                    } else {
                        reDraw("can't go north");
                        repaintWall(Direction.UP);
                    }
                }
                case 'A' -> {
                    if (player.moveWest(theWorld)) {
                        operations.append("A");
                        reDraw("went west");
                        handleTeleport();
                    } else {
                        reDraw("can't go west");
                        repaintWall(Direction.LEFT);
                    }
                }
                case 'S' -> {
                    if (player.moveSouth(theWorld)) {
                        operations.append("S");
                        reDraw("went south");
                        handleTeleport();
                    } else {
                        reDraw("can't go south");
                        repaintWall(Direction.DOWN);
                    }
                }
                case 'D' -> {
                    if (player.moveEast(theWorld)) {
                        operations.append("D");
                        reDraw("went east");
                        handleTeleport();
                    } else {
                        reDraw("can't go east");
                        repaintWall(Direction.RIGHT);
                    }
                }
                case ':' -> {
                    reDraw("waiting for keyboard input");
                    while (true) {
                        if (!StdDraw.hasNextKeyTyped()) {
                            continue;
                        }
                        char ch = Character.toUpperCase(StdDraw.nextKeyTyped());
                        if (ch == 'Q') {
                            reDraw("saving...");
                            saveOperations();
                            System.exit(0);
                        }
                        break;
                    }
                }
                default -> {
                }
            }
        }
    }

    private void handleTeleport() {
        if (atPortal()) {
            repaint(player.getPosition());
            teleport();
            repaint(player.getPosition());
            StdDraw.pause(500);
            reDraw("teleport !");
        }
    }

    private void teleport() {
        Position current = player.getPosition();
        Position destination = null;
        while (destination == null) {
            Position candidate = portals.get(RandomUtils.uniform(random, portals.size()));
            if (!candidate.equals(current)) {
                destination = candidate;
            }
        }
        player.moveTo(theWorld, destination);
    }

    private void repaint(Position position) {
        var x = position.getX();
        var y = position.getY();
        TETile tile = theWorld[x][y];
        theWorld[x][y] = TETile.colorVariant(tile, 60, 60, 60, random);
    }

    private boolean atPortal() {
        return portals.contains(player.getPosition());
    }

    private void reDraw(String gameInfo) {
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 14));
        ter.renderFrameWithShadow(theWorld, player.getPosition(), 15);
        renderHUD(gameInfo);
    }

    private Long parseSeed(String interactions) {
        String upper = interactions.toUpperCase();
        var indexOfN = upper.indexOf("N");
        var indexOfS = upper.indexOf("S");
        return Long.parseLong(upper.substring(indexOfN + 1, indexOfS));
    }

    private String loadOperations() {
        try {
            return Files.readString(Paths.get("byow", "Core", "save.txt"));
        } catch (IOException e) {
            return "";
        }
    }

    private void saveOperations() {
        var path = Paths.get("byow", "Core", "save.txt");
        try (var stream = new BufferedOutputStream(Files.newOutputStream(path))) {
            stream.write(String.valueOf(operations).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    private String solicitSeed() {
        char endFlag = 'S';
        StringBuilder seedBuilder = new StringBuilder();
        while (true) {
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
            char input = StdDraw.nextKeyTyped();
            if (Character.isDigit(input)) {
                seedBuilder.append(input);
                StdDraw.clear(Color.BLACK);
                setGameTitle();
                StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
                StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0 + 1, seedBuilder.toString());
                StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0 - 1, "Please entry a random number");
                StdDraw.show();
            } else if (Character.toUpperCase(input) == endFlag) {
                return seedBuilder.toString();
            }
        }
    }

    private void promptToSeedUi() {
        StdDraw.clear(Color.BLACK);
        setGameTitle();
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0 - 1, "Please entry a random number");
        StdDraw.show();
    }

    private String solicitBeginningStr() {
        StringBuilder beginningStr = new StringBuilder();
        while (beginningStr.length() != 1) {
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
            char input = Character.toUpperCase(StdDraw.nextKeyTyped());
            if (input == 'N' || input == 'L' || input == 'Q') {
                beginningStr.append(input);
            }
        }
        return beginningStr.toString();
    }

    private void showMainMenu() {
        initCanvas();
        setGameTitle();
        setGameOptions();

        StdDraw.show();
    }

    private void setGameOptions() {
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0 + 2, "New Game (N)");
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "Load Game (L)");
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0 - 2, "Quit (Q)");
    }

    private void setGameTitle() {
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 60));
        StdDraw.text(WIDTH / 2.0, HEIGHT * 3 / 4.0, "CS61B:  THE GAME");
    }

    private void initCanvas() {
        ter.initialize(WIDTH, HEIGHT + 2);
        StdDraw.setPenColor(Color.WHITE);
    }

    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */
    public void interactWithKeyboard() {

        //1. show the starting UI
        showMainMenu();

        //2. wait for and read input(only N、L、Q is valid)
        String beginningStr = solicitBeginningStr();

        if (beginningStr.equals("N")) {
            newGame();
        } else if (beginningStr.equals("L")) {
            loadGame();
            interact();
        } else {
            saveOperations();
            System.exit(0);
        }
    }

    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called interactWithInputString with the string "l",
     * we'd expect to get the exact same world back again, since this corresponds to
     * loading the saved game.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        var upper = input.toUpperCase();
        var delimitation = upper.indexOf("S") + 1;
        var quitIndex = upper.indexOf(":Q");

        if (upper.startsWith("N")) {
            long seed = parseSeed(input);
            operations = new StringBuilder("N" + seed + "S");
            theWorld = generateWorld(seed);
            if (quitIndex >= 0) {
                processMovementStr(upper.substring(delimitation, quitIndex));
            } else {
                processMovementStr(upper.substring(delimitation));
            }
            saveOperations();
        } else if (upper.startsWith("L")) {
            operations = new StringBuilder(loadOperations());
            if (operations.length() == 0) {
                System.exit(0);
            }
            theWorld = interactWithInputString(String.valueOf(operations));
            if (quitIndex >= 0) {
                processMovementStr(upper.substring(1, quitIndex));
                System.exit(0);
            } else {
                if (upper.length() > 1) {
                    processMovementStr(upper.substring(1));
                }
            }
            saveOperations();
        } else {
            throw new IllegalStateException("illegal input: " + input);
        }
        return theWorld;
    }

    private void processMovementStr(String movement) {
        if (movement.length() == 0) {
            return;
        }
        switch (movement.substring(0, 1)) {
            case "W" -> {
                if (player.moveNorth(theWorld)) {
                    operations.append("W");
                    if (atPortal()) {
                        teleport();
                    }
                }
            }
            case "A" -> {
                if (player.moveWest(theWorld)) {
                    operations.append("A");
                    if (atPortal()) {
                        teleport();
                    }
                }
            }
            case "S" -> {
                if (player.moveSouth(theWorld)) {
                    operations.append("S");
                    if (atPortal()) {
                        teleport();
                    }
                }
            }
            case "D" -> {
                if (player.moveEast(theWorld)) {
                    operations.append("D");
                    if (atPortal()) {
                        teleport();
                    }
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + movement.charAt(0));
        }
        processMovementStr(movement.substring(1));
    }

    /**
     * Intern routine to generate the 2D tile game world, pseudo-randomly.
     *
     * @param seed pseudo-random seed
     * @return the world generated
     * @source <a href="https://zhuanlan.zhihu.com/p/27381213">...</a>
     */
    private TETile[][] generateWorld(long seed) {
        //1. init
        theWorld = new TETile[WIDTH][HEIGHT];
        for (TETile[] column : theWorld) {
            Arrays.fill(column, Tileset.NOTHING);
        }
        random = new Random(seed);

        //2. generate rooms pseudo-randomly
        List<Room> rooms = generateRooms();

        //3. generate halls from button left of the map
        Position start = new Position(1, 1);
        theWorld[1][1] = Tileset.FLOOR;
        boolean[][] visited = new boolean[WIDTH][HEIGHT];
        List<Position> deadEnds = halls(start, new ArrayList<>(), visited);

        //4. connect rooms and halls
        connectRoomsAndHalls(rooms);

        //5. subtract deadEnds pseudo-randomly
        removeDeadEnds(deadEnds, new ArrayList<>(deadEnds.size()));

        //6. generate walls
        generateWalls();

        //7. set beginning position
        setBeginning();

        //8. repaint dead ends and rooms into floors
        repaint(rooms);

        //9. set portals
        setPortals();

        return theWorld;
    }

    private void setPortals() {
        var numOfPortals = 4;
        portals = new ArrayList<>(numOfPortals);
        while (portals.size() < numOfPortals) {
            var x = RandomUtils.uniform(random, 1, WIDTH);
            var y = RandomUtils.uniform(random, 1, HEIGHT);
            if (theWorld[x][y].equals(Tileset.FLOOR)) {
                theWorld[x][y] = Tileset.IN;
                var portal = new Position(x, y);
                portals.add(portal);
            }
        }
    }

    private void repaint(List<Room> rooms) {
        if (rooms != null && rooms.size() != 0) {
            for (Room room : rooms) {
                repaint(room);
            }
        }
    }

    private void repaint(Room room) {
        var xStart = room.getButtonLeft().getX();
        var xEnd = room.getTopRight().getX();
        var yStart = room.getButtonLeft().getY();
        var yEnd = room.getTopRight().getY();

        for (var x = xStart; x <= xEnd; x++) {
            for (var y = yStart; y <= yEnd; y++) {
                theWorld[x][y] = Tileset.FLOOR;
            }
        }
    }

    private void setBeginning() {
        Position beginning = null;
        while (beginning == null) {
            var x = RandomUtils.uniform(random, 1, WIDTH);
            var y = RandomUtils.uniform(random, 1, HEIGHT);
            var candidate = new Position(x, y);
            List<Position> walls = candidate.oddNeighbours(theWorld, Tileset.WALL);
            if (theWorld[x][y].equals(Tileset.FLOOR) && walls.size() > 0) {
                beginning = walls.get(RandomUtils.uniform(random, walls.size()));
            }
        }
        theWorld[beginning.getX()][beginning.getY()] = Tileset.LOCKED_DOOR;
        player = new Player(beginning);
    }

    private void generateWalls() {
        for (var x = 0; x < theWorld.length; x++) {
            for (var y = 0; y < theWorld[0].length; y++) {
                if (isWall(x, y)) {
                    theWorld[x][y] = TETile.colorVariant(Tileset.WALL, 30, 30, 30, random);
                }
            }
        }
    }

    private boolean isWall(int x, int y) {
        if (!theWorld[x][y].equals(Tileset.NOTHING)) {
            return false;
        }
        var current = new Position(x, y);
        List<Position> floors = current.diagonalNeighbours(theWorld, Tileset.FLOOR);
        List<Position> rooms = current.diagonalNeighbours(theWorld, Tileset.ROOM);
        return floors.size() + rooms.size() > 0;
    }

    private void removeDeadEnds(List<Position> deadEnds, List<Position> newEnds) {
        if (deadEnds == null || deadEnds.size() == 0) {
            return;
        }
        deadEnds.forEach(deadEnd -> {
            List<Position> neighbours = deadEnd.oddNeighbours(theWorld, Tileset.FLOOR);
            if (neighbours.size() == 1 && RandomUtils.bernoulli(random, 0.985)) {
                theWorld[deadEnd.getX()][deadEnd.getY()] = Tileset.NOTHING;
                removeDeadEnds(neighbours, newEnds);
            } else if (deadEnd.oddNeighbours(theWorld, Tileset.FLOOR).size() == 1) {
                newEnds.add(deadEnd);
            }
        });
    }


    private void connectRoomsAndHalls(List<Room> rooms) {
        for (Room room : rooms) {
            connectRoomsAndHalls(room);
        }
    }

    private void connectRoomsAndHalls(Room room) {
        var buttonRight = new Position(room.getTopRight().getX(), room.getButtonLeft().getY());
        var topLeft = new Position(room.getButtonLeft().getX(), room.getTopRight().getY());

        connectRoomsAndHalls(room.getButtonLeft(), buttonRight, Direction.DOWN);
        connectRoomsAndHalls(room.getButtonLeft(), topLeft, Direction.LEFT);
        connectRoomsAndHalls(topLeft, room.getTopRight(), Direction.UP);
        connectRoomsAndHalls(buttonRight, room.getTopRight(), Direction.RIGHT);
    }

    private void connectRoomsAndHalls(Position start, Position end, Direction direction) {
        List<Position> candidates = new ArrayList<>(4);
        Position connector, candidate;

        if (direction.equals(Direction.DOWN) || direction.equals(Direction.UP)) {
            var y = direction.equals(Direction.DOWN) ? start.getY() - 2 : start.getY() + 2;
            for (var x = start.getX(); x <= end.getX(); x++) {
                candidate = new Position(x, y);
                if (!candidate.outOf(theWorld) && theWorld[x][y].equals(Tileset.FLOOR)) {
                    candidates.add(candidate);
                }
            }

            connector = connector(candidates);
            if (connector != null) {
                var yPrim = y < start.getY() ? y + 1 : y - 1;
                theWorld[connector.getX()][yPrim] =
                        TETile.colorVariant(Tileset.FLOOR, 30, 30, 30, random);
            }
        } else {
            var connectorX = direction.equals(Direction.LEFT) ? start.getX() - 2 : start.getX() + 2;
            for (var y = start.getY(); y <= end.getY(); y++) {
                candidate = new Position(connectorX, y);
                if (!candidate.outOf(theWorld) && theWorld[connectorX][y].equals(Tileset.FLOOR)) {
                    candidates.add(candidate);
                }
            }

            connector = connector(candidates);
            if (connector != null) {
                var bridgeX = connectorX < start.getX() ? connectorX + 1 : connectorX - 1;
                theWorld[bridgeX][connector.getY()] =
                        TETile.colorVariant(Tileset.FLOOR, 30, 30, 30, random);
            }
        }
    }

    private Position connector(List<Position> candidates) {
        Position connector = null;
        if (candidates.size() != 0 && RandomUtils.bernoulli(random, 0.7)) {
            var luckyNum = RandomUtils.uniform(random, candidates.size());
            connector = candidates.get(luckyNum);
        }
        return connector;
    }


    private List<Position> halls(Position start, List<Position> deadEnds, boolean[][] visited) {
        if (start.outOf(theWorld) || visited[start.getX()][start.getY()]) {
            return deadEnds;
        }
        visited[start.getX()][start.getY()] = true;
        List<Position> neighbours = start.evenNeighbours(theWorld, Tileset.NOTHING);

        if (neighbours.size() == 0) {
            deadEnds.add(start);
            return deadEnds;
        }

        while (neighbours.size() > 0) {
            var randomIndex = RandomUtils.uniform(random, neighbours.size());
            Position neighbour = neighbours.remove(randomIndex);
            if (!visited[neighbour.getX()][neighbour.getY()]) {
                connectPositions(start, neighbour);
            }
            halls(neighbour, deadEnds, visited);
        }

        return deadEnds;
    }

    private void connectPositions(Position start, Position neighbour) {
        var connectorX = (start.getX() + neighbour.getX()) / 2;
        var connectorY = (start.getY() + neighbour.getY()) / 2;

        theWorld[neighbour.getX()][neighbour.getY()] =
                TETile.colorVariant(Tileset.FLOOR, 30, 30, 30, random);
        theWorld[connectorX][connectorY] =
                TETile.colorVariant(Tileset.FLOOR, 30, 30, 30, random);
    }

    private List<Room> generateRooms() {
        List<Room> rooms = new ArrayList<>();
        var roomNums = RandomUtils.uniform(random, 8, 13);
        while (rooms.size() < roomNums) {
            Room newRoom = Room.randomRoom(random, theWorld);
            if (!newRoom.overLap(rooms) && !newRoom.adjacent(rooms)) {
                rooms.add(newRoom);
                fillWithRoomTile(newRoom);
            }
        }
        return rooms;
    }

    private void fillWithRoomTile(Room room) {
        var xStart = room.getButtonLeft().getX();
        var xEnd = room.getTopRight().getX();
        var yStart = room.getButtonLeft().getY();
        var yEnd = room.getTopRight().getY();

        for (var x = xStart; x < xEnd + 1; x++) {
            TETile[] column = theWorld[x];
            Arrays.fill(column, yStart, yEnd + 1,
                    TETile.colorVariant(Tileset.ROOM, 30, 30, 30, random));
        }
    }
}
