package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import dataaccess.SQLUserDAO;
import model.AuthData;
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
        return;
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
}
