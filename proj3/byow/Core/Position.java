package byow.Core;

import byow.TileEngine.TETile;

import java.util.ArrayList;
import java.util.List;

public class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        Position another = (Position) o;
        return x == another.getX() && y == another.getY();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(x) + Integer.hashCode(y);
    }

    public boolean belongsTo(TETile[][] theWorld, TETile type) {
        return theWorld[x][y].equals(type);
    }

    public boolean outOf(TETile[][] theWorld) {
        int width = theWorld.length;
        int height = theWorld[0].length;
        return (x <= 0 || x >= width || y <= 0 || y >= height);
    }

    public List<Position> oddNeighbours(TETile[][] theWorld, TETile neighbourType) {
        return neighbours(theWorld, neighbourType, false, false);
    }


    public List<Position> evenNeighbours(TETile[][] theWorld, TETile neighbourType) {
        return neighbours(theWorld, neighbourType, true, false);
    }

    private List<Position> neighbours(TETile[][] tiles, TETile type, boolean even, boolean across) {
        List<Position> neighbours = new ArrayList<>(4);
        int width = tiles.length;
        int height = tiles[0].length;
        int delta = even ? 2 : 1;

        Position left = new Position(x - delta, y);
        Position right = new Position(x + delta, y);
        Position top = new Position(x, y + delta);
        Position button = new Position(x, y - delta);

        Position topLeft = new Position(x - delta, y + delta);
        Position topRight = new Position(x + delta, y + delta);
        Position buttonLeft = new Position(x - delta, y - delta);
        Position buttonRight = new Position(x + delta, y - delta);

        if (x > delta && left.belongsTo(tiles, type)) {
            neighbours.add(left);
        }
        if (x < width - delta && right.belongsTo(tiles, type)) {
            neighbours.add(right);
        }
        if (y > delta && button.belongsTo(tiles, type)) {
            neighbours.add(button);
        }
        if (y < height - delta && top.belongsTo(tiles, type)) {
            neighbours.add(top);
        }

        if (across) {
            if (x > delta && y > delta && buttonLeft.belongsTo(tiles, type)) {
                neighbours.add(buttonLeft);
            }
            if (x < width - delta && y > delta && buttonRight.belongsTo(tiles, type)) {
                neighbours.add(buttonRight);
            }
            if (x > delta && y < height - delta && topLeft.belongsTo(tiles, type)) {
                neighbours.add(topLeft);
            }
            if (x < width - delta && y < height - delta && topRight.belongsTo(tiles, type)) {
                neighbours.add(topRight);
            }
        }

        return neighbours;
    }

    public List<Position> diagonalNeighbours(TETile[][] theWorld, TETile neighbourType) {
        return neighbours(theWorld, neighbourType, false, true);
    }
}
