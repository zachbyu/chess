package dataaccess;

import model.GameData;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

public interface GameDAO {
    GameData getGame(int gameID)throws DataAccessException;
    GameData createGame(String gameName)throws DataAccessException;
    GameData updateGame(GameData game) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void clearGames()throws DataAccessException;
}
