package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import dataaccess.SQLUserDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;
import static websocket.messages.ServerMessage.ServerMessageType.*;

@WebSocket
public class WebSocketHandler {
    SQLUserDAO userDAO;
    public SQLAuthDAO authDAO = new SQLAuthDAO();
    public SQLGameDAO gameDAO = new SQLGameDAO();
    private Gson gson = new Gson();
    private final ConnectionHandler connections = new ConnectionHandler();

    @OnWebSocketMessage
    public void onMessage(Session session, String message)throws IOException, DataAccessException, SQLException, InvalidMoveException{
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        String authToken = command.getAuthToken();
        if(authToken == null | authToken.isEmpty()){
            connections.sendMessage(session, new ErrorMessage(ERROR, "Error: no authentication"));
            return;
        }
        AuthData auth = authDAO.getAuth(authToken);
        if(auth == null){
            connections.sendMessage(session, new ErrorMessage(ERROR, "Error: unauthorized"));
            return;
        }
        String username = auth.username();
        int gameID = command.getGameID();

        connections.add(gameID, username, session);

        switch (command.getCommandType()) {
            case CONNECT -> connect(session, username, command);
            case MAKE_MOVE -> makeMove(session, username, message);
            case LEAVE -> leaveGame(session, username, command);
            case RESIGN -> resign(session, username, command);
        }
    }

    private void connect(Session session, String username, UserGameCommand command) throws IOException, DataAccessException{
        System.out.println("connect command received from client");
        int gameID = command.getGameID();
        GameData gameData = getValidGameData(session, gameID);
        if (gameData == null) {return;}
        ChessGame game = gameData.game();
        //send out messages when connected
        connections.add(command.getGameID(), username, session);
        //who is joining?
        String playerType;
        if(gameData.whiteUsername()!=null && gameData.whiteUsername().equals(username)){
            playerType = "white";
        } else if(gameData.blackUsername()!=null && gameData.blackUsername().equals(username)){
            playerType = "black";
        } else{
            playerType = "observer";
        }
        //actually send message
        String message = username + " joined game " + gameID + " as " + playerType;
        NotificationMessage connectMessage = new NotificationMessage(NOTIFICATION, message);
        connections.broadcast(gameID, username, connectMessage);
        //load message back to root client
        LoadGameMessage gameMessage = new LoadGameMessage(LOAD_GAME, game, playerType);
        connections.sendMessage(session, gameMessage);
        System.out.println("just sent message");

    }

    private void makeMove(Session session, String username, String message)throws IOException, DataAccessException, InvalidMoveException{
        MakeMoveCommand moveCommand = gson.fromJson(message, MakeMoveCommand.class);
        ChessMove move = moveCommand.getChessMove();

        int gameID = moveCommand.getGameID();
        GameData gameData = getValidGameData(session, gameID);
        if(gameData == null){return;}

        ChessGame game = gameData.game();

        if(game.isGameOver()){
            connections.sendMessage(session, new ErrorMessage(ERROR, "Error: game is over, no moves may be made"));
        }

        ChessBoard board = game.getBoard();
        ChessGame.TeamColor currTurnColor = game.getTeamTurn();
        ChessGame.TeamColor otherTeamColor = null;
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        String playerType = "";

        if(currTurnColor != getPlayerColor(username, gameData)){
            connections.sendMessage(session, new ErrorMessage(ERROR, "Error: must be players turn"));
        }
        ChessPiece movePiece = board.getPiece(startPos);
        if(currTurnColor != movePiece.getTeamColor()){
            connections.sendMessage(session, new ErrorMessage(ERROR, "Error: cannot move piece you do not own"));
        }

        if(currTurnColor==WHITE){
            playerType="white";
            otherTeamColor = BLACK;
        }
        if(currTurnColor==BLACK){
            playerType="black";
            otherTeamColor=WHITE;
        }

        Collection<ChessMove> validMoves = game.validMoves(startPos);
        boolean moveValid=false;
        for (ChessMove possibleMove: validMoves){
            ChessPosition position = possibleMove.getEndPosition();
            if (position.equals(endPos)){
                moveValid = true;
            }
        }
        if(!moveValid){
            connections.sendMessage(session, new ErrorMessage(ERROR, "Error: move is not valid"));
            return;
        }
        //update game
        game.makeMove(move);
        GameData updatedGame = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
        gameDAO.updateGame(updatedGame);

        //send load message
        LoadGameMessage gameMessage = new LoadGameMessage(LOAD_GAME, game, playerType);
        connections.broadcast(gameID, "", gameMessage);

        //send notification message to all others saying what move was made
        String moveMessage = currTurnColor + " player " + username + " moved from " + move.getStartPosition() + " to " + move.getEndPosition();
        NotificationMessage moveNotification = new NotificationMessage(NOTIFICATION, moveMessage);
        connections.broadcast(gameID, username, moveNotification);

        //send extra notification messages
        if(game.isInCheck(otherTeamColor)){
            String checkMessage = otherTeamColor + " player " + username + " is in check";
            NotificationMessage checkNotification = new NotificationMessage(NOTIFICATION, checkMessage);
            connections.broadcast(gameID, "", checkNotification);
        }

        if(game.isInCheckmate(otherTeamColor)){
            String checkmateMessage = otherTeamColor + " player " + username + " is in checkmate. Game over.";
            NotificationMessage checkmateNotification = new NotificationMessage(NOTIFICATION, checkmateMessage);
            connections.broadcast(gameID, "", checkmateNotification);
            game.setGameOver(true);
        }

        if(game.isInStalemate(otherTeamColor)){
            String stalemateMessage = otherTeamColor + " player " + username + " is in stalemate. Game over.";
            NotificationMessage stalemateNotification = new NotificationMessage(NOTIFICATION, stalemateMessage);
            connections.broadcast(gameID, "", stalemateNotification);
            game.setGameOver(true);
        }
    }


    private void leaveGame(Session session, String username, UserGameCommand command) throws DataAccessException, IOException{
        return;
    }

    private void resign(Session session, String username, UserGameCommand command) throws  DataAccessException, IOException{
        return;
    }

    private GameData getValidGameData(Session session, int gameID) throws DataAccessException, IOException {
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            connections.sendMessage(session, new ErrorMessage(ERROR, "Error: enter a valid game ID"));
            return null;
        }
        return gameData;
    }

    private ChessGame.TeamColor getPlayerColor(String username, GameData game) {
        if(game.whiteUsername()!=null && game.whiteUsername().equals(username)) {
            return WHITE;
        }
        else if(game.blackUsername()!=null && game.blackUsername().equals(username)) {
            return BLACK;
        }
        else{
            return null;
        }
    }
}
