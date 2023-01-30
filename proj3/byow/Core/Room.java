package byow.Core;

import byow.TileEngine.TETile;

import java.util.List;
import java.util.Random;

public class Room {
    private final Position buttonLeft;
    private final Position topRight;

    private static final int CAP = 6;

    public Room(Position buttonLeftPosition, Position topRightPosition) {
        buttonLeft = buttonLeftPosition;
        topRight = topRightPosition;
    }

    public Position getButtonLeft() {
        return buttonLeft;
    }

    public Position getTopRight() {
        return topRight;
    }

    public static Room randomRoom(Random random, TETile[][] theWorld) {
        int height = theWorld[0].length;
        int width = theWorld.length;

        int x0 = RandomUtils.uniform(random, 3, width - CAP);
        int y0 = RandomUtils.uniform(random, 3, height - CAP);
        Position p0 = new Position(x0, y0);

        int x1 = RandomUtils.uniform(random, x0 + 1, x0 + CAP + 1);
        int y1 = RandomUtils.uniform(random, y0 + 1, y0 + CAP + 1);
        Position p1 = new Position(x1, y1);

        return new Room(p0, p1);
    }

    public boolean adjacent(Room another) {
        return adjacentAtTop(another)
                || adjacentAtButton(another)
                || adjacentAtLeft(another)
                || adjacentAtRight(another);
    }

    private boolean adjacentAtRight(Room another) {
        int xMax = topRight.getX();
        int yMin = buttonLeft.getY();
        int yMax = topRight.getY();

        int anotherXMin = another.buttonLeft.getX();
        int anotherYMin = another.buttonLeft.getY();
        int anotherYMax = another.topRight.getY();

        return xMax == anotherXMin - 1 && (yMin <= anotherYMax && yMax >= anotherYMin);
    }

    private boolean adjacentAtLeft(Room another) {
        int xMin = buttonLeft.getX();
        int yMin = buttonLeft.getY();
        int yMax = topRight.getY();

        int anotherXMax = another.topRight.getX();
        int anotherYMin = another.buttonLeft.getY();
        int anotherYMax = another.topRight.getY();

        return xMin == anotherXMax + 1 && (yMin <= anotherYMax && yMax >= anotherYMin);
    }

    private boolean adjacentAtTop(Room another) {
        int yMax = topRight.getY();
        int xMin = buttonLeft.getX();
        int xMax = topRight.getX();

        int anotherYMin = another.buttonLeft.getY();
        int anotherXMin = another.buttonLeft.getX();
        int anotherXMax = another.topRight.getX();

        return yMax == anotherYMin - 1 && (xMin <= anotherXMax && xMax >= anotherXMin);
    }

    private boolean adjacentAtButton(Room another) {
        int yMin = buttonLeft.getY();
        int xMin = buttonLeft.getX();
        int xMax = topRight.getX();

        int anotherYMax = another.topRight.getY();
        int anotherXMin = another.buttonLeft.getX();
        int anotherXMax = another.topRight.getX();

        return yMin == anotherYMax + 1 && (xMin <= anotherXMax && xMax >= anotherXMin);
    }

    public boolean adjacent(List<Room> rooms) {
        if (rooms == null || rooms.size() == 0) {
            return false;
        }
        for (Room room : rooms) {
            if (adjacent(room)) {
                return true;
            }
        }
        return false;
    }

    public boolean overLap(Room another) {
        if (another == null) {
            return false;
        }
        if (this.equals(another)) {
            return true;
        }
        return buttonLeft.getY() <= another.topRight.getY()
                && topRight.getY() >= another.buttonLeft.getY()
                && buttonLeft.getX() <= another.topRight.getX()
                && topRight.getX() >= another.buttonLeft.getX();
    }

    public boolean overLap(List<Room> rooms) {
        if (rooms == null || rooms.size() == 0) {
            return false;
        }
        for (Room room : rooms) {
            if (overLap(room)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if ((!(o instanceof Room))) {
            return false;
        }
        return buttonLeft.equals(((Room) o).buttonLeft) && topRight.equals(((Room) o).topRight);
    }

    @Override
    public int hashCode() {
        return buttonLeft.hashCode() + topRight.hashCode();
    }

    public boolean contains(Position p) {
        return buttonLeft.getX() <= p.getX() && p.getX() <= topRight.getX()
                && buttonLeft.getY() <= p.getY() && p.getY() <= topRight.getY();
    }
}
