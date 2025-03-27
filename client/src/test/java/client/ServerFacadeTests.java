package client;

import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;
import server.handlers.*;
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
        HttpRequest clearRequest = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/db"))
                .DELETE()
                .build();
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

    @Test
    public void loginTestValid() throws Exception{
        facade.register(new RegisterRequest("username", "password", "email"));
        LoginRequest request = new LoginRequest("username", "password");
        LoginResult result = facade.login(request);
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    public void loginTestInvalid() throws Exception{
        LoginRequest request = new LoginRequest("username", null);
        assertThrows(Exception.class, ()-> facade.login(request));
    }

    @Test
    public void logoutTestValid() throws Exception{
        RegisterResult result = facade.register(new RegisterRequest("username", "password", "email"));
        String auth = result.authToken();
        facade.logout(auth);
        assertThrows(Exception.class, ()-> facade.logout(auth));
    }

    @Test
    public void logoutTestInvalid() throws Exception{
        RegisterResult result = facade.register(new RegisterRequest("username", "password", "email"));
        assertThrows(Exception.class, ()->facade.logout("notatoken"));
    }

    @Test
    public void createTestValid() throws Exception {
        RegisterResult registered = facade.register(new RegisterRequest("username", "32", "email"));
        //not sure if this authtoken works or not
        CreateGameResult result = facade.createGame(new CreateGameRequest("myGame"));
        assertTrue(result.gameID() > 0);
    }

    @Test
    public void createTestInvalid() throws Exception{
        assertThrows(Exception.class, ()->facade.createGame(new CreateGameRequest("myGame")));
    }

}
