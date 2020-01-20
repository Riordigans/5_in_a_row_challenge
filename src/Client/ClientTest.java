package Client;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClientTest extends Thread {

    HttpServer server;
    static Socket clientSocket;
    static ServerSocket serverSocket;
    final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Test
    public void testJoinGame() throws IOException, InterruptedException {
        System.setOut(new PrintStream(outContent));
        server = HttpServer.create(new InetSocketAddress(80), 0);
        serverSocket = new ServerSocket(8080);
        ClientHttp client = new ClientHttp();

        //Test that the joinGame method issues a http GET and returns the value of the token key
        //Also test that joinGame prints the body of the http response
        server.createContext("/startGame", new GameStartHandler("Token", "test1"));
        server.start();

        String token = client.joinGame();

        String response = outContent.toString();

        assertTrue(token.equals("test1"));
        assertTrue(response.equals("GET\r\n"));

        server.stop(0);
        serverSocket.close();
    }

    @Test
    public void testNameToServer() throws IOException, InterruptedException {
        System.setOut(new PrintStream(outContent));
        server = HttpServer.create(new InetSocketAddress(80), 0);
        serverSocket = new ServerSocket(8080);
        ClientHttp client = new ClientHttp();

        //Test that the nameToServer method issues a http PUT with the passed String as a body
        //Also test that nameToServer prints the body of the http response
        server.createContext("/startGame", new GameStartHandler("Token", "test1"));
        server.createContext("/setName", new SetNameHandler("", ""));
        server.start();
        String testName = "test";

        client.token = client.joinGame();

        client.nameToServer(testName);
        String response = outContent.toString();

        assertTrue(response.contains("PUT\r\n" + testName));

        server.stop(0);
        serverSocket.close();
    }

    @Test
    public void testPlayGame() throws IOException, InterruptedException {
        System.setOut(new PrintStream(outContent));
        server = HttpServer.create(new InetSocketAddress(80), 0);
        serverSocket = new ServerSocket(8080);
        ByteArrayInputStream inContent = new ByteArrayInputStream("1".getBytes());
        System.setIn(inContent);
        ClientHttp client = new ClientHttp();

        //Test playGame returns false immediately if the EndGame header is not false
        server.createContext("/startGame", new GameStartHandler("Token", "test1"));
        HashMap<String, List<String>> httpHeaders = new HashMap();
        httpHeaders.put("EndGame", Arrays.asList("true"));
        httpHeaders.put("Turn", Arrays.asList("true"));
        server.createContext("/playGame", new PlayGameHandler(httpHeaders));
        server.start();

        client.token = client.joinGame();

        boolean res = client.playGame();
        assertFalse(res);

        //Test playGame makes a PUT immediately after a successful GET
        server.removeContext("/startGame");
        server.removeContext("/playGame");
        server.createContext("/startGame", new GameStartHandler("Token", "test1"));
        httpHeaders.clear();
        httpHeaders.put("EndGame", Arrays.asList("false"));
        httpHeaders.put("Turn", Arrays.asList("true"));
        server.createContext("/playGame", new PlayGameHandler(httpHeaders));

        client.token = client.joinGame();

        res = client.playGame();
        String response = outContent.toString();

        assertTrue(response.contains("GET\r\nPUT"));
        //Assert that playGame returns true after a move is made and game is not over
        assertTrue(res);

        server.stop(0);
        serverSocket.close();
    }

    static class GameStartHandler implements HttpHandler {
        String key, value;

        public GameStartHandler(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public void handle(final HttpExchange t) throws IOException {
            Headers responseHeaders = t.getResponseHeaders();
            responseHeaders.put(key, Arrays.asList(value));

            clientSocket = serverSocket.accept();

            String response = t.getRequestMethod();
            t.sendResponseHeaders(200, response.getBytes().length);
            final OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class SetNameHandler implements HttpHandler {
        String key, value;

        public SetNameHandler(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public void handle(final HttpExchange t) throws IOException {
            Headers responseHeaders = t.getResponseHeaders();
            responseHeaders.put(key, Arrays.asList(value));
            String body = new String(t.getRequestBody().readAllBytes());

            String response = t.getRequestMethod() + "\r\n" + body;
            t.sendResponseHeaders(200, response.getBytes().length);
            final OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class PlayGameHandler implements HttpHandler {
        Map headers;

        public PlayGameHandler(Map headers) {
            this.headers = headers;
        }

        @Override
        public void handle(final HttpExchange t) throws IOException {
            Headers responseHeaders = t.getResponseHeaders();
            responseHeaders.putAll(headers);

            clientSocket.getOutputStream().write(1);

            String response = t.getRequestMethod();
            t.sendResponseHeaders(200, response.getBytes().length);
            final OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
