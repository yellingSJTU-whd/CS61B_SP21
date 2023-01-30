package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Player {
    private Position position;

    public Position getPosition() {
        return position;
    }

    public Player(Position place) {
        position = new Position(place.getX(), place.getY());
    }

    public boolean moveNorth(TETile[][] theWorld) {
        return move(theWorld, Direction.UP);
    }

    public boolean moveWest(TETile[][] theWorld) {
        return move(theWorld, Direction.LEFT);
    }

    public boolean moveSouth(TETile[][] theWorld) {
        return move(theWorld, Direction.DOWN);
    }

    public boolean moveEast(TETile[][] theWorld) {
        return move(theWorld, Direction.RIGHT);
    }

    private boolean move(TETile[][] theWorld, Direction direction) {
        int currentX = position.getX();
        int currentY = position.getY();
        int width = theWorld.length;
        int height = theWorld[0].length;
        int xPrim = currentX;
        int yPrim = currentY;

        switch (direction) {
            case UP:
                yPrim++;
                break;
            case LEFT:
                xPrim--;
                break;
            case DOWN:
                yPrim--;
                break;
            case RIGHT:
                xPrim++;
                break;
            default:
        }

        if (xPrim <= 0 || xPrim >= width || yPrim < 0 || yPrim >= height
                || !(theWorld[xPrim][yPrim].equals(Tileset.FLOOR)
                || theWorld[xPrim][yPrim].equals(Tileset.IN))) {
            return false;
        }

        if (theWorld[currentX][currentY].equals(Tileset.LOCKED_DOOR)) {
            theWorld[currentX][currentY] = Tileset.UNLOCKED_DOOR;
        } else if (theWorld[currentX][currentY].equals(Tileset.OUT)) {
            theWorld[currentX][currentY] = Tileset.IN;
        } else {
            theWorld[currentX][currentY] = Tileset.FLOOR;
        }

        if (theWorld[xPrim][yPrim].equals(Tileset.FLOOR)) {
            theWorld[xPrim][yPrim] = Tileset.AVATAR;
        }

        position = new Position(xPrim, yPrim);
        return true;
    }

    public void moveTo(TETile[][] theWorld, Position destination) {
        int x = destination.getX();
        int y = destination.getY();
        position = new Position(x, y);
        if (theWorld[x][y].equals(Tileset.IN)) {
            theWorld[x][y] = Tileset.OUT;
        }
    }
}
