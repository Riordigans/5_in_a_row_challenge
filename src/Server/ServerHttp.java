package Server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/*
Class ServerHttp
This class implements a http server that runs the game and allows clients to connect
*/
public class ServerHttp {

    private Game game = new Game();
    private PlayerState playerState = new PlayerState();
    private Socket[] clientSocket = new Socket[2];
    private final int HttpPort = 80;
    private ServerSocket keepAlive;
    private final int keepAlivePort = 8080;
    private HttpServer server;

    //Need a keepalive port to check if players disconnect
    public ServerHttp() {
        try {
            keepAlive = new ServerSocket(keepAlivePort);
            server = HttpServer.create(new InetSocketAddress(HttpPort), 0);
        } catch (IOException e) {
            System.out.println("Error starting server. Ensure there are no other instances running.");
        }
    }

    //Set up the three server APIs
    public static void main(final String[] args) {
        ServerHttp serverHttp = new ServerHttp();
        serverHttp.server.createContext("/startGame", new GameStartHandler(serverHttp));
        serverHttp.server.createContext("/setName", new SetNameHandler(serverHttp));
        serverHttp.server.createContext("/playGame", new PlayGameHandler(serverHttp));
        serverHttp.server.setExecutor(null); // creates a default executor
        serverHttp.server.start();
    }

    static class GameStartHandler implements HttpHandler {
        ServerHttp server;

        public GameStartHandler(ServerHttp serverHttp) {
            server = serverHttp;
        }

        @Override
        public void handle(final HttpExchange t) throws IOException {

            Headers responseHeaders = t.getResponseHeaders();

            String response = server.startGame(responseHeaders);

            t.sendResponseHeaders(200, response.getBytes().length);
            final OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class SetNameHandler implements HttpHandler {
        ServerHttp server;

        public SetNameHandler(ServerHttp serverHttp) {
            server = serverHttp;
        }

        @Override
        public void handle(final HttpExchange t) throws IOException {

            String name = new String(t.getRequestBody().readAllBytes());
            String token = t.getRequestHeaders().get("Token").get(0);

            String response = server.getName(name, token);

            t.sendResponseHeaders(200, response.getBytes().length);
            final OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class PlayGameHandler implements HttpHandler {
        ServerHttp server;

        public PlayGameHandler(ServerHttp serverHttp) {
            server = serverHttp;
        }

        @Override
        public void handle(final HttpExchange t) throws IOException {

            Headers responseHeaders = t.getResponseHeaders();
            String reqToken = t.getRequestHeaders().get("Token").get(0);
            String requestMethod = t.getRequestMethod();
            String body = new String(t.getRequestBody().readAllBytes());

            String response = server.playGame(responseHeaders, reqToken, requestMethod, body);

            t.sendResponseHeaders(200, response.getBytes().length);
            final OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    void newGame() {
        playerState = new PlayerState();
        game = new Game();
        System.out.println("Game finished.");
        System.out.println("Game will restart when new clients connect.");
    }

    String startGame(Headers responseHeaders) {
        int numPlayers = playerState.getNumPlayers();
        String response = "";
        boolean exFlag = false;

        System.out.println("Player attempting to join game...");

        //Allow up to two players to join the game
        if (numPlayers < 2 && !exFlag) {
            // Use socket to check if player disconnects
            try {
                // Give client one second to respond
                keepAlive.setSoTimeout(1000);
                clientSocket[numPlayers] = keepAlive.accept();
            } catch (Exception e) {
                response = "There's something wrong with this client...\r\n";
                response += "Please use the supplied game client only.";
                System.out.println("Player refused.");
                exFlag = true;
            }

            //Allow each player to join the game, and set a token to keep track of the requests
            //coming from each player.
            if (!exFlag) {
                numPlayers++;
                response = "You are Player " + numPlayers + ".";
                if (numPlayers == 1) {
                    response += "\r\nYou go first.";
                }
                if (numPlayers == 2) {
                    response += "\r\nYou go second.";
                }
                // Could randomly generate this, keeping it simple for now
                String playerToken = "player" + numPlayers;
                playerState.addPlayer(new Player(numPlayers, playerToken));
                responseHeaders.put("Token", Arrays.asList(playerToken));
                System.out.println("Player joined.");
            }

        } else if (!exFlag) {
            response = "There are already 2 players in the game, wait your turn.";
            responseHeaders.put("Token", Arrays.asList("noPlay"));
            System.out.println("Player refused.");
        }
        return response;
    }

    public String getName(String name, String token) {
        System.out.println("Player name:" + name);
        String response = "";

        if (playerState.getAllTokens().contains(token)) {
            playerState.addPlayerName(token, name);
            response = "Hello " + name + ", welcome to 5-in-a-row!\r\n";

            if (playerState.getNumPlayers() == 1) {
                response += "Waiting for Player 2 to connect...";
            } else if (playerState.getNumPlayers() == 2) {
                response += "All players connected. Game starting...";
            }
        } else {
            response = "The token supplied by the client is invalid.";
        }
        return response;
    }

    //Main method to handle moves being PUT from the client and the client requesting GETs for the
    //current game state
    String playGame(Headers responseHeaders, String reqToken, String requestMethod, String body) {
        String response = "";

        Player currentPlayer = playerState.currentPlayer();
        String name = currentPlayer.getName();
        String playerToken = currentPlayer.getToken();
        boolean exFlag = false;

        responseHeaders.put("EndGame", Arrays.asList("false"));

        // Wait for both players to connect before returning game data
        if (playerState.getNumPlayers() != 2) {
            response = "NotYourTurn";
            responseHeaders.put("Turn", Arrays.asList("false"));
        } else {
            Player otherPlayer = playerState.otherPlayer();
            // Check if game has been won/lost by other player
            if (game.checkVictory(otherPlayer.getNum()) && !exFlag) {
                responseHeaders.put("EndGame", Arrays.asList("true"));
                response = game.toString();
                response += "Sorry" + name + ", you lose! " + otherPlayer.getName() + " is the winner.";
                // Once both players have been notified that the game is over, restart
                newGame();
            } else if (game.boardFull() && !exFlag) {
                responseHeaders.put("EndGame", Arrays.asList("tie"));
                response = game.toString();
                response += "The board is full, it's a tie!";
                // Once both players have been notified that the game is over, restart
                newGame();
            } else {
                // Check for disconnect by writing to sockets
                for (Socket s : clientSocket) {
                    try {
                        OutputStream os = s.getOutputStream();
                        os.write(1);
                        os.flush();
                    } catch (IOException e) {
                        responseHeaders.put("EndGame", Arrays.asList("true"));
                        response += "\r\nA player has disconnected, the game is over.";
                        exFlag = true;
                        // Prepare for a new game
                        newGame();
                    }
                }

                //Handle GET as part of regular game flow
                if (reqToken.equals(playerToken)) {
                    if (requestMethod.equals("GET") && !exFlag) {
                        response = playGET(responseHeaders);
                    } else if (requestMethod.equals("PUT") && !exFlag) {
                        response = playPUT(reqToken, body, responseHeaders);
                    }
                } else {
                    response = "NotYourTurn";
                    responseHeaders.put("Turn", Arrays.asList("false"));
                }
            }
        }
        return response;
    }

    //GET returns the game state if it is the current player's go. Prints out the game board
    //and offers them to make a move
    String playGET(Headers responseHeaders) {
        String response = "";
        Player current = playerState.currentPlayer();
        String name = current.getName();

        response = game.toString();
        response += "It's your turn " + name + ", please enter a column (1-9):";
        responseHeaders.put("Turn", Arrays.asList("true"));

        return response;
    }

    //PUT allows the current player to make a move. They include a column number in the PUT
    //that the piece will get played in.
    String playPUT(String reqToken, String body, Headers responseHeaders) {
        String response = "";
        boolean exFlag = false;
        Player current = playerState.currentPlayer();
        String name = current.getName();
        int playerNum = current.getNum();

        int move = 0;

        try {
            move = Integer.parseInt(body);
        } catch (NumberFormatException nfEx) {
            response = "That's not a valid number.";
            exFlag = true;
        }
        if (!exFlag) {
            try {
                game.newMove(playerNum, move);
            } catch (InvalidMoveException e) {
                response = e.getMessage();
                exFlag = true;
            }
        }

        //Check for victory condition, if the game is still live
        if (game.checkVictory(playerNum) && !exFlag) {
            responseHeaders.put("EndGame", Arrays.asList("true"));
            response = "Congratulations " + name + ", you have won!";
            playerState.nextPlayer();
        } else if (game.boardFull()) {
            responseHeaders.put("EndGame", Arrays.asList("true"));
            response = "The board is full, it's a tie!";
            playerState.nextPlayer();
        } else if (!exFlag) {
            Player nextPlayer = playerState.nextPlayer();
            String nextPlayerName = nextPlayer.getName();

            response = game.toString();

            if (nextPlayerName.isEmpty()) {
                response += "\r\nMove submitted, waiting for the other player to make their move.";
            } else {
                response += "\r\nMove submitted, waiting for " + nextPlayerName + " to make their move.";
            }
        }
        return response;
    }

    // Included for testing purposes
    // junit tests attempt to reinitialise server between tests
    void close() throws Exception {
        keepAlive.close();
        server.stop(0);
    }
}