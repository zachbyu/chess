package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.GameData;
import server.handlers.*;

import java.util.ArrayList;

public class GameService {
    private final UserDAO userDataAccess;
    private final AuthDAO authDataAccess;
    private final GameDAO gameDataAccess;

    public GameService(UserDAO userDataAccess, AuthDAO authDataAccess, GameDAO gameDataAccess){
        this.userDataAccess = userDataAccess;
        this.authDataAccess = authDataAccess;
        this.gameDataAccess = gameDataAccess;
    }
    public CreateGameResult createGame(CreateGameRequest createGameRequest)throws DataAccessException {
        GameData game = gameDataAccess.createGame(createGameRequest.gameName());
        CreateGameResult result = new CreateGameResult(game.gameID());
        return result;
    }

    public ListGamesResult listGames()throws DataAccessException{
        ListGamesResult games = new ListGamesResult(gameDataAccess.listGames());
        return games;
    }

    public void clearGames()throws DataAccessException{
        gameDataAccess.clearGames();
    }


}

