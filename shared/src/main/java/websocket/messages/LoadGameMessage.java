package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage{

    private final ChessGame game;
    private final String playerType;

    public LoadGameMessage(ServerMessageType type, ChessGame game, String playerType){
        super(type);
        this.game = game;
        this.playerType = playerType;
    }

    public ChessGame getGame(){return game;}

    public String getPlayerType() {return playerType;}

}