package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.GameData;
import server.handlers.*;

import java.util.ArrayList;
import java.util.Objects;

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

    public void joinGame(JoinGameRequest joinGameRequest, String username)throws DataAccessException{
        String color = joinGameRequest.playerColor();
        GameData game = gameDataAccess.getGame(joinGameRequest.gameID());
        if (game == null){
            throw  new DataAccessException(400, "Error: bad request");
        }
        if (Objects.equals(color, "WHITE")){
            if (game.whiteUsername() != null){
                throw new DataAccessException(403, "Error: already taken");
            }
            else{
                GameData newGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
                gameDataAccess.updateGame(newGame);
            }
        }
        else if (Objects.equals(color, "BLACK")){
            if (game.blackUsername() != null){
                throw new DataAccessException(403, "Error: already taken");
            }
            else{
                GameData newGame = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
                gameDataAccess.updateGame(newGame);
            }
        }else{
            throw new DataAccessException(400, "Error: bad request");
        }

    }



}

