package ui.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static websocket.commands.UserGameCommand.CommandType.*;
import static websocket.messages.ServerMessage.ServerMessageType.ERROR;

public class WebSocketFacade extends Endpoint {
    Session session;
    ServerMessageObserver observer;
    Gson gson = new Gson();

    public WebSocketFacade(String url, ServerMessageObserver observer) throws Exception{
        try{
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "ws");
            this.observer = observer;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    observer.notify(message);
                }
            });
        }catch(Exception ex){
            throw new Exception(ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void observeGame(String authToken, int gameID)throws Exception{
        try{
            UserGameCommand observeCommand = new UserGameCommand(CONNECT, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(observeCommand));
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

}
