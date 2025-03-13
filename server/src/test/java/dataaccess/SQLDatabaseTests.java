
package dataaccess;

import chess.ChessGame;
import server.*;
import model.*;
import org.junit.jupiter.api.*;
import org.opentest4j.AssertionFailedError;
import service.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLDatabaseTests {

    SQLUserDAO sqlUserDataAccess = new SQLUserDAO();
    SQLAuthDAO sqlAuthDataAccess = new SQLAuthDAO();
    SQLGameDAO sqlGameDataAccess = new SQLGameDAO();
    UserService userService = new UserService(sqlUserDataAccess, sqlAuthDataAccess);
    AuthService authService = new AuthService(sqlUserDataAccess, sqlAuthDataAccess);
    GameService gameService = new GameService(sqlUserDataAccess, sqlAuthDataAccess, sqlGameDataAccess);

    @BeforeAll
    public static void init() {
    }


    @Test
    public void createAuthSuccess() throws DataAccessException, SQLException {
        AuthData newAuth = new AuthData("authToken123", "user123");

        AuthData createdAuth = sqlAuthDataAccess.createAuth(newAuth);
        System.out.println("Auth created: " + createdAuth);

        AuthData retrievedAuth = sqlAuthDataAccess.getAuth("authToken123");
        System.out.println("Retrieved auth: " + retrievedAuth);

        Assertions.assertNotNull("Auth should be created", String.valueOf(createdAuth));
        Assertions.assertNotNull("Auth should be retrieved", String.valueOf(retrievedAuth));
        Assertions.assertEquals(retrievedAuth.authToken(),"authToken123",
                "Response did not give the same auth as expected");
    }

    @Test
    public void getAuthSuccess() throws DataAccessException, SQLException {
        AuthData newAuth = new AuthData("authToken456", "user456");

        sqlAuthDataAccess.createAuth(newAuth);

        AuthData retrievedAuth = sqlAuthDataAccess.getAuth("authToken456");
        System.out.println("Retrieved auth: " + retrievedAuth);

        Assertions.assertNotNull("Auth should be retrieved", String.valueOf(retrievedAuth)); // Check if the AuthData is retrieved
        Assertions.assertEquals("authToken456", retrievedAuth.authToken()); // Ensure the authToken matches
        Assertions.assertEquals("user456", retrievedAuth.username()); // Ensure the username matches
    }

    @Test
    public void getAuthFail() throws SQLException, DataAccessException {
        AuthData newAuth = new AuthData("authToken11", "user11");

        sqlAuthDataAccess.createAuth(newAuth);
        AuthData retrievedAuth = sqlAuthDataAccess.getAuth("authToken22");

        NullPointerException e = Assertions.assertThrows(NullPointerException.class, () ->
                Assertions.assertEquals(retrievedAuth.username(), "user33",
                        "Response did not give the same gameID as expected"));
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException, SQLException {
        AuthData newAuth = new AuthData("authTokenDeleteTest", "userDeleteTest");
        sqlAuthDataAccess.createAuth(newAuth);
        sqlAuthDataAccess.deleteAuth("authTokenDeleteTest");

        AuthData retrievedAuth = sqlAuthDataAccess.getAuth("authTokenDeleteTest");

        Assertions.assertNull(retrievedAuth, "Auth should be deleted"); // Check if retrievedAuth is null
    }

    @Test
    public void deleteAuthFail() throws DataAccessException, SQLException {
        AuthData newAuth = new AuthData("authTokenDeleteTest", "userDeleteTest");
        sqlAuthDataAccess.createAuth(newAuth);
        sqlAuthDataAccess.deleteAuth("authTokenDeleteTest");

        AuthData retrievedAuth = sqlAuthDataAccess.getAuth("authTokenDeleteTest");

        NullPointerException e = Assertions.assertThrows(NullPointerException.class, () ->
                Assertions.assertEquals(retrievedAuth.username(), "userDeleteTest",
                        "Response could not retrieve deleted authToken"));
    }

    @Test
    public void clearAuthSuccess() throws SQLException, DataAccessException {
        sqlAuthDataAccess.clearAuths();
    }

}
