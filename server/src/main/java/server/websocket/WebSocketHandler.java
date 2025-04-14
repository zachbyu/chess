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
import static websocket.messages.ServerMessage.ServerMessageType.*;

public class WebSocketHandler {
    SQLUserDAO userDAO;
    public SQLAuthDAO authDAO;
    public SQLGameDAO gameDAO;
    private Gson gson = new Gson();
    private final ConnectionHandler connections = new ConnectionHandler();

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
        return;
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
}
