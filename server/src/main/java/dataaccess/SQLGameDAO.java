package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.SQLException;
import java.util.ArrayList;

public class SQLGameDAO implements GameDAO{

    public SQLGameDAO() {
        try { DatabaseManager.createDatabase(); } catch (DataAccessException ex) {
            throw new RuntimeException(ex);
        }
        try (var conn = DatabaseManager.getConnection()) {
            var createTestTable = """            
                    CREATE TABLE if NOT EXISTS game (
                                    gameID INT NOT NULL,
                                    whiteUsername VARCHAR(255),
                                    blackUsername VARCHAR(255),
                                    gameName VARCHAR(255),
                                    chessGame TEXT,
                                    PRIMARY KEY (gameID)
                                    )""";
            try (var createTableStatement = conn.prepareStatement(createTestTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        String chessGameString = new Gson().toJson(game.game());
        try (var conn = DatabaseManager.getConnection()){
            try(var statement = conn.prepareStatement("INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, chessGame) VALUES(?,?,?,?,?)")){
                statement.setInt(1, game.gameID());
                statement.setString(2, game.whiteUsername());
                statement.setString(3, game.blackUsername());
                statement.setString(4, game.gameName());
                statement.setString(5, chessGameString);
                statement.executeUpdate();
            }
        }catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
        return new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()){
            try(var statement = conn.prepareStatement("SELECT whiteUsername, blackUsername, gameName, chessGame FROM game WHERE gameID=?")){
                statement.setInt(1, gameID);
                try(var results = statement.executeQuery()){
                    results.next();
                    var whiteUsername = results.getString("whiteUsername");
                    var blackUsername = results.getString("blackUsername");
                    var gameName = results.getString("gameName");
                    var chessGame = new Gson().fromJson(results.getString("chessGame"), ChessGame.class);
                    return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                }
            }
        }catch (SQLException e){
            throw new DataAccessException("Game not found: " + gameID);
        }
    }

    @Override
    public GameData updateGame(GameData game) throws DataAccessException {
        return null;
    }

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException {
        return null;
    }

    @Override
    public void clearGames() throws DataAccessException {

    }
}
