package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static websocket.messages.ServerMessage.ServerMessageType.ERROR;

public class ConnectionHandler {
    private final Map<Integer, Map<String,Session>> connections = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void add(int gameID, String username, Session session){
        connections.putIfAbsent(gameID, new ConcurrentHashMap<>());
        connections.get(gameID).put(username, session);
    }

    public void remove(int gameID, String username) {
        if(connections.containsKey(gameID)){
            if(connections.get(gameID).containsKey(username)){
                connections.get(gameID).remove(username);
            }
            if(connections.get(gameID).isEmpty()){
                connections.remove(gameID);
            }
        }
    }

    public void sendMessage(Session session, ServerMessage message) throws IOException {
        if(session.isOpen()){
            session.getRemote().sendString(gson.toJson(message));
        }
    }

    public void broadcast(int gameID, String excludeUsername, ServerMessage message) throws IOException{
        if (!connections.containsKey(gameID)){
            return;
        }else{
            var gameConnections = connections.get(gameID);
            System.out.println("in broadcast, going to exclude"+excludeUsername);
            for (var user:gameConnections.keySet()){
                if(!user.equals(excludeUsername)){
                    System.out.println("in broadcast, sending message to " + user);
                    Session currSession = gameConnections.get(user);
                    sendMessage(currSession, message);
                }
            }
        }
    }
}
