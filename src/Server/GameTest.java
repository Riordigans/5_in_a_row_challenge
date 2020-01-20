package Server;

import org.junit.Test;

import static org.junit.Assert.*;

public class GameTest {

    @Test
    public void testColumnNumberForNewMoveInvalid() {
        Game game = new Game();

        //Test move too low
        try {
            game.newMove(1, 0);
            fail("Should have failed on column number too low");
        } catch (Exception exc) {
            assertTrue(exc.getClass().equals(InvalidMoveException.class));
        }

        //Test move too high
        try {
            game.newMove(2, 10);
            fail("Should have failed on column number too high");
        } catch (Exception exc) {
            assertTrue(exc.getClass().equals(InvalidMoveException.class));
        }
    }

    @Test
    public void testColumnFull() {
        Game game = new Game();

        for (int i = 0; i < 6; i++) {
            try {
                game.newMove(1, 1);
            } catch (Exception exc) {
                fail("Moves should be valid");
            }
        }

        try {
            game.newMove(1, 1);
            fail("Column should be full");
        } catch (Exception exc) {
            assertTrue(exc.getClass().equals(InvalidMoveException.class));
        }
    }

    @Test
    public void testVerticalVictory() throws InvalidMoveException {
        Game game = new Game();
        for (int i = 0; i < 4; i++) {
            game.newMove(1, 1);
        }
        assertFalse(game.checkVictory(1));
        game.newMove(1, 1);
        assertTrue(game.checkVictory(1));
        assertFalse(game.checkVictory(2));
    }

    @Test
    public void testHorizontalVictory() throws InvalidMoveException {
        Game game = new Game();
        for (int i = 1; i < 5; i++) {
            game.newMove(2, i);
        }
        assertFalse(game.checkVictory(2));
        game.newMove(2, 5);
        assertTrue(game.checkVictory(2));
        assertFalse(game.checkVictory(1));
    }

    @Test
    public void testForwardDiagonalVictory() throws InvalidMoveException {
        Game game = new Game();

        for (int i = 1; i <= 4; i++) {
            for (int j = 1; j <= i; j++) {
                game.newMove(1, i);
            }
            game.newMove(2, 5);
        }

        assertFalse(game.checkVictory(1));
        game.newMove(1, 5);
        assertTrue(game.checkVictory(1));
        assertFalse(game.checkVictory(2));
    }

    @Test
    public void testBackwardDiagonalVictory() throws InvalidMoveException {
        Game game = new Game();

        for (int i = 2; i <= 5; i++) {
            for (int j = (6 - i); j >= 1; j--) {
                game.newMove(1, i);
            }
            game.newMove(2, 1);
        }

        assertFalse(game.checkVictory(1));
        game.newMove(1, 1);
        assertTrue(game.checkVictory(1));
        assertFalse(game.checkVictory(2));
    }

    @Test
    public void testBoardFull() throws InvalidMoveException {
        Game game = new Game();
        assertFalse(game.boardFull());

        for (int i = 1; i <= 9; i++) {
            for (int j = 0; j < 6; j++) {
                game.newMove(1, i);
            }
        }
        assertTrue(game.boardFull());
    }
}
