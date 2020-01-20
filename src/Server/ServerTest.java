package Server;

import com.sun.net.httpserver.Headers;
import org.junit.Test;

import java.net.Socket;

import static org.junit.Assert.assertTrue;

public class ServerTest extends Thread {
    ServerHttp server = new ServerHttp();
    Headers h1 = new Headers();

    @Test
    public void testStartGame() throws InterruptedException {
        //Testing a socket is required to join
        String returned = server.startGame(h1);
        assertTrue(returned.contains("There's something wrong"));

        //Testing first player joined successfully
        socketThread.run();
        returned = server.startGame(h1);
        socketThread.join();

        assertTrue(returned.contains("You are Player 1"));

        //Testing second player joined successfully
        socketThread.run();
        returned = server.startGame(h1);
        socketThread.join();

        assertTrue(returned.contains("You are Player 2"));

        //Testing only two players can be connected at once
        socketThread.run();
        returned = server.startGame(h1);
        socketThread.join();

        assertTrue(returned.contains("There are already 2 players in the game"));
        try {
            server.close();
        } catch (Exception e) {
        }
        ;
    }

    @Test
    public void testGetName() throws InterruptedException {
        socketThread.run();
        server.startGame(h1);
        socketThread.join();

        //Testing with invalid token
        String returned = server.getName("testName", "p1");

        assertTrue(returned.contains("The token supplied by the client is invalid."));

        //Testing only one player in game with valid token
        returned = server.getName("testName", "player1");

        assertTrue(returned.contains("Hello testName") && returned.contains("Waiting for Player 2"));

        //Testing two players in game with valid tokens
        socketThread.run();
        server.startGame(h1);
        socketThread.join();

        returned = server.getName("testName", "player2");
        assertTrue(returned.contains("Hello testName") && returned.contains("All players connected."));
        try {
            server.close();
        } catch (Exception e) {
        }
        ;
    }

    @Test
    public void testPlayGame() throws InterruptedException {
        //Not testing end game states
        //Test that server ends game on disconnect
        socketThread.run();
        server.startGame(h1);
        socketThread.join();

        disconnectingSocketThread.run();
        server.startGame(h1);
        disconnectingSocketThread.join();

        //Two queries required due to buffering
        server.playGame(h1, "player1", "GET", "");
        String returned = server.playGame(h1, "player1", "GET", "");
        assertTrue(returned.contains("A player has disconnected"));

        //Test that server waits for both players to join
        socketThread.run();
        server.startGame(h1);
        socketThread.join();

        returned = server.playGame(h1, "player1", "GET", "");
        assertTrue(returned.contains("NotYourTurn"));

        //Test that server allows player to get game state once both have joined
        socketThread.run();
        server.startGame(h1);
        socketThread.join();

        returned = server.playGame(h1, "player1", "GET", "");
        assertTrue(returned.contains("It's your turn"));

        //Test that server won't allow invalid tokens to get or put
        returned = server.playGame(h1, "invalid", "GET", "");
        assertTrue(returned.contains("NotYourTurn"));

        returned = server.playGame(h1, "invalid", "PUT", "4");
        assertTrue(returned.contains("NotYourTurn"));

        //Test that server won't allow non-integer values for move
        returned = server.playGame(h1, "player1", "PUT", "");
        assertTrue(returned.contains("That's not a valid number."));

        //Test that server won't allow invalid integers for move
        returned = server.playGame(h1, "player1", "PUT", "34");
        assertTrue(returned.contains("Invalid Move"));

        //Test that server accepts valid move
        returned = server.playGame(h1, "player1", "PUT", "4");
        assertTrue(returned.contains("Move submitted"));

        //Test that valid move selects next player
        returned = server.playGame(h1, "player1", "PUT", "4");
        assertTrue(returned.contains("NotYourTurn"));

        returned = server.playGame(h1, "player2", "PUT", "4");
        assertTrue(returned.contains("Move submitted"));

        try {
            server.close();
        } catch (Exception e) {
        }
        ;
    }

    @Test
    public void testNewGame() throws InterruptedException {
        //Test that players are properly added to playerState
        socketThread.run();
        server.startGame(h1);
        socketThread.join();

        socketThread.run();
        server.startGame(h1);
        socketThread.join();

        socketThread.run();
        String returned = server.startGame(h1);
        socketThread.join();

        assertTrue(returned.contains("There are already 2 players in the game"));

        //Test that newGame() removes all players from playerState
        server.newGame();

        socketThread.run();
        returned = server.startGame(h1);
        socketThread.join();

        assertTrue(returned.contains("You are Player 1"));

        try {
            server.close();
        } catch (Exception e) {
        }
        ;
    }

    Thread socketThread = new Thread() {
        public void run() {
            try {
                new Socket("127.0.0.1", 8080);
            } catch (Exception e) {

            }
        }
    };

    Thread disconnectingSocketThread = new Thread() {
        public void run() {
            try {
                Socket socket = new Socket("127.0.0.1", 8080);
                socket.close();
            } catch (Exception e) {

            }
        }
    };

}
