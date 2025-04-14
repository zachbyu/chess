package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand{
    private final ChessMove move;
    private String startPosistion;
    private String endPosition;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID,
                           ChessMove move, String startPosistion, String endPosition){
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
        this.startPosistion = startPosistion;
        this.endPosition = endPosition;
    }

    public ChessMove getChessMove(){
        return move;
    }
    public String getStartPosition(){
        return startPosistion;
    }

    public String getEndPosition(){
        return endPosition;
    }
}
