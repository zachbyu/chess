package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import handlers.CreateGameRequest;
import handlers.CreateGameResult;
import handlers.JoinGameRequest;
import handlers.ListGamesResult;
import model.GameData;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class GameService {
    private final GameDAO gameDataAccess;

    public GameService(UserDAO userDataAccess, AuthDAO authDataAccess, GameDAO gameDataAccess){
        this.gameDataAccess = gameDataAccess;
    }
    public CreateGameResult createGame(CreateGameRequest createGameRequest)throws DataAccessException {
        int gameID;
        do {
            gameID = ThreadLocalRandom.current().nextInt(1, 10000);
        }while(gameDataAccess.gameExists(gameID));

        GameData game = new GameData(gameID, null, null, createGameRequest.gameName(), new ChessGame());
        GameData insertedGame = gameDataAccess.createGame(game);
        return new CreateGameResult(insertedGame.gameID());
    }

    public ListGamesResult listGames()throws DataAccessException{
        return new ListGamesResult(gameDataAccess.listGames());
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

