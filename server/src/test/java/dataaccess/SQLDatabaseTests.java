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
public class SQLDatabaseTests{

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
    public void createUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("user1", "password1", "email1");

        UserData createdUser = sqlUserDataAccess.createUser(newUser);
        //System.out.println("User created: " + createdUser);
        Assertions.assertNotNull("User should be created", String.valueOf(createdUser));
    }

    @Test
    public void createUserFail() throws DataAccessException {
        UserData newUser = new UserData("user11", "password11", ""); // Empty email simulates failure

        UserData createdUser = sqlUserDataAccess.createUser(newUser);
        Assertions.assertNotNull("User should not be created", String.valueOf(createdUser)); // This assertion will fail, as we expect a null value
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("user2", "password2", "email2");

        UserData createdUser = sqlUserDataAccess.createUser(newUser);
        UserData retrievedUser = sqlUserDataAccess.getUser("user2");

        Assertions.assertNotNull("User should be retrieved", String.valueOf(retrievedUser));
        Assertions.assertEquals(retrievedUser.username(), "user2",
                "Response did not give the same gameID as expected");
    }

    @Test
    public void getUserFail() throws DataAccessException {
        UserData newUser = new UserData("user22222", "password22222", "email22222");

        UserData createdUser = sqlUserDataAccess.createUser(newUser);
        UserData retrievedUser = sqlUserDataAccess.getUser("user22222");

        AssertionFailedError e = Assertions.assertThrows(AssertionFailedError.class, () ->
                Assertions.assertEquals(retrievedUser.username(), "user33",
                        "Response did not give the same gameID as expected"));
    }

    @Test
    public void clearUserSuccess() throws SQLException, DataAccessException {
        sqlUserDataAccess.clearUsers();
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
                        ""));
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
    public void clearAuthSuccess() throws SQLException, DataAccessException {
        sqlAuthDataAccess.clearAuths();
    }

    @Test
    public void createGameSuccess() throws SQLException, DataAccessException {
        ChessGame chessGame = new ChessGame();

        GameData game = new GameData(1, null, null, "game1name", chessGame);
        GameData newGame = sqlGameDataAccess.createGame(game);

        GameData retrievedGame = sqlGameDataAccess.getGame(1);

        Assertions.assertNotNull("Game should be created", String.valueOf(newGame));
        Assertions.assertNotNull("User should be retrieved", String.valueOf(retrievedGame));
    }

    @Test
    public void createGameFail() throws SQLException, DataAccessException {
        ChessGame chessGame = new ChessGame();

        GameData game = new GameData(1000, null, null, "game1name", chessGame);
        GameData newGame = sqlGameDataAccess.createGame(game);

        GameData retrievedGame = sqlGameDataAccess.getGame(1000);

        Assertions.assertNotNull("Game should be created", String.valueOf(newGame));
        Assertions.assertNotNull("Game should be retrieved", String.valueOf(retrievedGame));
        Assertions.assertEquals(retrievedGame.gameName(), "game1name", "game name didn't match");
    }


    @Test
    public void updateGameSuccess() throws SQLException, DataAccessException {
        ChessGame chessGame = new ChessGame();

        GameData game = new GameData(1001, null, null, "game1name", chessGame);
        GameData newGame = sqlGameDataAccess.createGame(game);

        GameData updatedData = new GameData(1001, "newWhite", "newBlack", "newName", chessGame);
        GameData updatedGame = sqlGameDataAccess.updateGame(updatedData);

        Assertions.assertEquals(updatedData, updatedGame, "game did not update correctly");
    }


    @Test
    public void updateGameFail() throws SQLException, DataAccessException {
        ChessGame chessGame = new ChessGame();

        GameData game = new GameData(3, null, null, "game1name", chessGame);
        GameData newGame = sqlGameDataAccess.createGame(game);

        GameData updatedData = new GameData(1, "newWhite", "newBlack", "newName", chessGame);
        GameData updatedData2 = new GameData(4, "newWhite", "newBlack", "newName", chessGame);
        GameData updatedGame = sqlGameDataAccess.updateGame(updatedData);

        AssertionFailedError e = Assertions.assertThrows(AssertionFailedError.class, () ->
                Assertions.assertEquals(updatedData2, updatedGame, "game did not update correctly"));
    }

    @Test
    public void listGamesSuccess() throws DataAccessException, SQLException {
        ChessGame chessGame = new ChessGame();

        GameData game1 = new GameData(2001, "white1", "black1", "game1name", chessGame);
        GameData newGame1 = sqlGameDataAccess.createGame(game1);
        GameData game2 = new GameData(2002, "white2", "black2", "game2name", chessGame);
        GameData newGame2 = sqlGameDataAccess.createGame(game2);

        ArrayList<GameData> games = sqlGameDataAccess.listGames();

        Assertions.assertTrue(games.size() > 1, "Not enough games in the list");

        Assertions.assertEquals(games.get(0).gameID(), 2001,
                "Response did not give the same gameID as expected");
        Assertions.assertEquals(games.get(1).gameID(), 2002,
                "Response did not give the same gameID as expected");
    }


    @Test
    public void listGamesFail() throws DataAccessException, SQLException {
        ChessGame chessGame = new ChessGame();

        GameData game1 = new GameData(3001, "white1", "black1", "game1name", chessGame);
        GameData newGame1 = sqlGameDataAccess.createGame(game1);
        GameData game2 = new GameData(3002, "white2", "black2", "game2name", chessGame);
        GameData newGame2 = sqlGameDataAccess.createGame(game2);

        ArrayList<GameData> games = sqlGameDataAccess.listGames();


        AssertionFailedError e = Assertions.assertThrows(AssertionFailedError.class, () ->
                Assertions.assertEquals(games.get(0).gameID(), 9999, "Response did not give the expected gameID at index 0"));
        e = Assertions.assertThrows(AssertionFailedError.class, () ->
                Assertions.assertEquals(games.get(1).gameID(), 8888, "Response did not give the expected gameID at index 1"));

    }


    @Test
    public void clearGameSuccess() throws SQLException, DataAccessException {
        sqlGameDataAccess.clearGames();
    }

}