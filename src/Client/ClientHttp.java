package Client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.InputMismatchException;
import java.util.Scanner;

/*
Class ClientHttp
This class implements the game client that communicates with the server
*/
public class ClientHttp {

    String token;
    private String hostAddr = "127.0.0.1";
    private String uriString = "http://" + hostAddr;
    private Scanner userInput = new Scanner(System.in);
    private Socket keepAlive;
    private InputStream keepAliveIS;
    private int keepAlivePort = 8080;

    private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    //Creates new HTTP client, user can join localhost or a specific address to play remotely.
    //Calls functions as described in the rest of this class
    public static void main(String[] args) {
        try {
            ClientHttp obj = new ClientHttp();

            System.out.println("Welcome to 5-in-a-row! Enter the Server Address (or press Enter for localhost):");
            String usrServAdd = obj.userInput.nextLine();
            if (!usrServAdd.equals("")) {
                obj.hostAddr = usrServAdd;
                obj.uriString = "http://" + obj.hostAddr;
            }

            obj.token = obj.joinGame();
            if (obj.token.equals("noPlay"))
                return;
            System.out.println("Please enter your name:");
            String name = obj.userInput.nextLine();
            obj.nameToServer(name);
            while (obj.playGame())
                ;
            System.out.println("Game ended, terminating client.");
            obj.keepAlive.close();
        } catch (Exception e) {
            System.out.println("Error communicating with the server. Ensure address is entered correctly.");
        }
    }

    //Join game is initial request to the server. Checks for players present already and returns a success
    //if there is room for the current client to join the game.
    String joinGame() throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(uriString + "/startGame")).build();

        keepAlive = new Socket(hostAddr, keepAlivePort);
        keepAliveIS = keepAlive.getInputStream();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // print response body
        System.out.println(response.body());

        return response.headers().allValues("Token").get(0);
    }

    //If the client joins successfully, the server will allow them to enter their name. This name is stored in
    //the Player object.
    void nameToServer(String name) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder().PUT(ofFormString(name)).uri(URI.create(uriString + "/setName"))
                .header("Token", token).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // print response body
        System.out.println(response.body());
    }

    //Main game method. Player one is allowed to do the initial PUT request of a move to the server, and waits
    //500 milliseconds to try to GET the latest game state.
    boolean playGame() throws IOException, InterruptedException {

        boolean yourTurn = false;
        while (!yourTurn) {

            HttpRequest getRequest = HttpRequest.newBuilder().GET().uri(URI.create(uriString + "/playGame"))
                    .header("Token", token).build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            keepAliveIS.read();

            if (!getResponse.headers().allValues("EndGame").contains("false")) {
                System.out.println(getResponse.body());
                return false;
            }

            if (getResponse.headers().allValues("Turn").contains("false")) {
                // wait 500 ms before checking for your turn again
                Thread.sleep(500);
            } else {
                // print response body
                System.out.println(getResponse.body());
                yourTurn = true;
            }
        }

        //Input validation for valid move
        int move = 0;
        boolean validInt = false;
        while (!validInt) {
            try {
                move = userInput.nextInt();
                if (move >= 1 && move <= 9) {
                    validInt = true;
                } else {
                    System.out.println("That's not a valid move, try again!");
                }
            } catch (InputMismatchException e) {
                userInput.nextLine();
                System.out.println("That's not an integer, try again!");
            }
        }

        HttpRequest putRequest = HttpRequest.newBuilder().PUT(ofFormInt(move)).uri(URI.create(uriString + "/playGame"))
                .header("Token", token).build();

        HttpResponse<String> putResponse = httpClient.send(putRequest, HttpResponse.BodyHandlers.ofString());

        keepAliveIS.read();

        // print response body
        System.out.println(putResponse.body());
        if (!putResponse.headers().allValues("EndGame").contains("false")) {
            return false;
        }
        return true;
    }

    HttpRequest.BodyPublisher ofFormString(String str) {
        return HttpRequest.BodyPublishers.ofString(str);
    }

    HttpRequest.BodyPublisher ofFormInt(int num) {
        String numString = "" + num;
        return HttpRequest.BodyPublishers.ofString(numString);
    }
}
