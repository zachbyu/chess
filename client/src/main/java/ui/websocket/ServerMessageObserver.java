package ui.websocket;

import websocket.messages.ServerMessage;

public interface ServerMessageObserver {
    void notify(String serverMessage);
}