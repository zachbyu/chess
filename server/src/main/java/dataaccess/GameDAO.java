package dataaccess;

import model.GameData;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

public interface GameDAO {
    GameData getGame(int gameID)throws DataAccessException;
    GameData createGame(GameData game)throws DataAccessException;
    GameData updateGame(GameData game) throws DataAccessException;
    ArrayList<GameData> listGames() throws DataAccessException;
    void clearGames()throws DataAccessException;

    boolean gameExists(int gameID);
}
