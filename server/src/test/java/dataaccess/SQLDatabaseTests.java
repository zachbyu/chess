package dataaccess;

import chess.ChessGame;
import model.*;
import org.junit.jupiter.api.*;
import org.opentest4j.AssertionFailedError;
import service.*;

import java.util.ArrayList;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLDatabaseTests{

    SQLUserDAO sqlUserDAO = new SQLUserDAO();
    SQLAuthDAO sqlAuthDAO = new SQLAuthDAO();
    SQLGameDAO sqlGameDAO = new SQLGameDAO();
    UserService userService = new UserService(sqlUserDAO, sqlAuthDAO);
    AuthService authService = new AuthService(sqlUserDAO, sqlAuthDAO);
    GameService gameService = new GameService(sqlUserDAO, sqlAuthDAO, sqlGameDAO);

    @BeforeAll
    public static void init() {
    }

    @Test
    public void createUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("user1", "password1", "email1");

        UserData createdUser = sqlUserDAO.createUser(newUser);
        //System.out.println("User created: " + createdUser);
        Assertions.assertNotNull("User should be created", String.valueOf(createdUser));
    }

    @Test
    public void createUserFail() throws DataAccessException {
        UserData newUser = new UserData("user11", "password11", ""); // Empty email simulates failure

        UserData createdUser = sqlUserDAO.createUser(newUser);
        Assertions.assertNotNull("User should not be created", String.valueOf(createdUser)); // This assertion will fail, as we expect a null value
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("user2", "password2", "email2");

        UserData createdUser = sqlUserDAO.createUser(newUser);
        UserData retrievedUser = sqlUserDAO.getUser("user2");

        Assertions.assertNotNull("User should be retrieved", String.valueOf(retrievedUser));
        Assertions.assertEquals(retrievedUser.username(), "user2",
                "Response did not give the same gameID as expected");
    }

    @Test
    public void getUserFail() throws DataAccessException {
        UserData newUser = new UserData("user22222", "password22222", "email22222");

        UserData createdUser = sqlUserDAO.createUser(newUser);
        UserData retrievedUser = sqlUserDAO.getUser("user22222");

        AssertionFailedError e = Assertions.assertThrows(AssertionFailedError.class, () ->
                Assertions.assertEquals(retrievedUser.username(), "user33",
                        "Response did not give the same gameID as expected"));
    }

    @Test
    public void clearUserSuccess() throws DataAccessException {
        sqlUserDAO.clearUsers();
    }

    @Test
    public void createAuthSuccess() throws DataAccessException{
        AuthData newAuth = new AuthData("authToken123", "user123");

        AuthData createdAuth = sqlAuthDAO.createAuth(newAuth);
        System.out.println("Auth created: " + createdAuth);

        AuthData retrievedAuth = sqlAuthDAO.getAuth("authToken123");
        System.out.println("Retrieved auth: " + retrievedAuth);

        Assertions.assertNotNull("Auth should be created", String.valueOf(createdAuth));
        Assertions.assertNotNull("Auth should be retrieved", String.valueOf(retrievedAuth));
        Assertions.assertEquals(retrievedAuth.authToken(),"authToken123",
                "Response did not give the same auth as expected");
    }

    @Test
    public void getAuthSuccess() throws DataAccessException{
        AuthData newAuth = new AuthData("authToken456", "user456");

        sqlAuthDAO.createAuth(newAuth);

        AuthData retrievedAuth = sqlAuthDAO.getAuth("authToken456");
        System.out.println("Retrieved auth: " + retrievedAuth);

        Assertions.assertNotNull("Auth should be retrieved", String.valueOf(retrievedAuth)); // Check if the AuthData is retrieved
        Assertions.assertEquals("authToken456", retrievedAuth.authToken()); // Ensure the authToken matches
        Assertions.assertEquals("user456", retrievedAuth.username()); // Ensure the username matches
    }

    @Test
    public void getAuthFail() throws DataAccessException {
        AuthData newAuth = new AuthData("authToken11", "user11");

        sqlAuthDAO.createAuth(newAuth);
        AuthData retrievedAuth = sqlAuthDAO.getAuth("authToken22");

        NullPointerException e = Assertions.assertThrows(NullPointerException.class, () ->
                Assertions.assertEquals(retrievedAuth.username(), "user33",
                        ""));
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException{
        AuthData newAuth = new AuthData("authTokenDeleteTest", "userDeleteTest");
        sqlAuthDAO.createAuth(newAuth);
        sqlAuthDAO.deleteAuth("authTokenDeleteTest");

        AuthData retrievedAuth = sqlAuthDAO.getAuth("authTokenDeleteTest");

        Assertions.assertNull(retrievedAuth, "Auth should be deleted"); // Check if retrievedAuth is null
    }


    @Test
    public void clearAuthSuccess() throws DataAccessException {
        sqlAuthDAO.clearAuths();
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        ChessGame chessGame = new ChessGame();

        GameData game = new GameData(1, null, null, "game1name", chessGame);
        GameData newGame = sqlGameDAO.createGame(game);

        GameData retrievedGame = sqlGameDAO.getGame(1);

        Assertions.assertNotNull("Game should be created", String.valueOf(newGame));
        Assertions.assertNotNull("User should be retrieved", String.valueOf(retrievedGame));
    }

    @Test
    public void createGameFail() throws DataAccessException {
        ChessGame chessGame = new ChessGame();

        GameData game = new GameData(1000, null, null, "game1name", chessGame);
        GameData newGame = sqlGameDAO.createGame(game);

        GameData retrievedGame = sqlGameDAO.getGame(1000);

        Assertions.assertNotNull("Game should be created", String.valueOf(newGame));
        Assertions.assertNotNull("Game should be retrieved", String.valueOf(retrievedGame));
        Assertions.assertEquals(retrievedGame.gameName(), "game1name", "game name didn't match");
    }


    @Test
    public void updateGameSuccess() throws DataAccessException {
        ChessGame chessGame = new ChessGame();

        GameData game = new GameData(1001, null, null, "game1name", chessGame);
        sqlGameDAO.createGame(game);

        GameData updatedData = new GameData(1001, "newWhite", "newBlack", "newName", chessGame);
        GameData updatedGame = sqlGameDAO.updateGame(updatedData);

        Assertions.assertEquals(updatedData, updatedGame, "game did not update correctly");
    }


    @Test
    public void updateGameFail() throws DataAccessException {
        ChessGame chessGame = new ChessGame();

        GameData game = new GameData(3, null, null, "game1name", chessGame);
        sqlGameDAO.createGame(game);

        GameData updatedData = new GameData(1, "newWhite", "newBlack", "newName", chessGame);
        GameData updatedData2 = new GameData(4, "newWhite", "newBlack", "newName", chessGame);
        GameData updatedGame = sqlGameDAO.updateGame(updatedData);

        AssertionFailedError e = Assertions.assertThrows(AssertionFailedError.class, () ->
                Assertions.assertEquals(updatedData2, updatedGame, "game did not update correctly"));
    }

    @Test
    public void listGamesSuccess() throws DataAccessException{
        ChessGame chessGame = new ChessGame();

        GameData game1 = new GameData(2001, "white1", "black1", "game1name", chessGame);
        sqlGameDAO.createGame(game1);
        GameData game2 = new GameData(2002, "white2", "black2", "game2name", chessGame);
        sqlGameDAO.createGame(game2);

        ArrayList<GameData> games = sqlGameDAO.listGames();

        Assertions.assertTrue(games.size() > 1, "Not enough games in the list");

        Assertions.assertEquals(games.get(0).gameID(), 2001,
                "Response did not give the same gameID as expected");
        Assertions.assertEquals(games.get(1).gameID(), 2002,
                "Response did not give the same gameID as expected");
    }


    @Test
    public void listGamesFail() throws DataAccessException{
        ChessGame chessGame = new ChessGame();

        GameData game1 = new GameData(3001, "white1", "black1", "game1name", chessGame);
        sqlGameDAO.createGame(game1);
        GameData game2 = new GameData(3002, "white2", "black2", "game2name", chessGame);
        sqlGameDAO.createGame(game2);

        ArrayList<GameData> games = sqlGameDAO.listGames();


        AssertionFailedError e = Assertions.assertThrows(AssertionFailedError.class, () ->
                Assertions.assertEquals(games.get(0).gameID(), 9999, "Response did not give the expected gameID at index 0"));
        e = Assertions.assertThrows(AssertionFailedError.class, () ->
                Assertions.assertEquals(games.get(1).gameID(), 8888, "Response did not give the expected gameID at index 1"));

    }


    @Test
    public void clearGameSuccess() throws DataAccessException {
        sqlGameDAO.clearGames();
    }

}