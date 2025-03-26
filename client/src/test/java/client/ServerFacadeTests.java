package client;

import org.junit.jupiter.api.*;
import server.Server;
import server.handlers.RegisterRequest;
import server.handlers.RegisterResult;
import ui.ServerFacade;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private ServerFacade facade;


    @BeforeEach
    public void setUp() throws Exception {
        server = new Server();
        int port = server.run(0);
        facade = new ServerFacade(port);

        //clear db
        HttpRequest clearRequest = HttpRequest.newBuilder().uri(new URI("http://localhost:" + port + "/db")).DELETE().build();
        HttpResponse<String> clearResponse = HttpClient.newHttpClient().send(clearRequest, HttpResponse.BodyHandlers.ofString());
    }

    @AfterEach
    public void breakDown(){
        server.stop();
    }


    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void registerTestValid() throws Exception{
        RegisterRequest request = new RegisterRequest("username", "password", "123@domain.com");
        RegisterResult result = facade.register(request);
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    public void registerTestInvalid() throws Exception{
        RegisterRequest request = new RegisterRequest("username", null, "123@domain.com");
        assertThrows(Exception.class, ()-> facade.register(request));
    }

}
