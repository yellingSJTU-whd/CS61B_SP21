package byow.Core;


import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RoomTest {

    @Test
    void overLap() {
        Room r1 = new Room(new Position(0, 0), new Position(10, 10));
        Room r2 = new Room(new Position(9, 9), new Position(15, 15));

        assertTrue(r1.overLap(r2));
    }
}
