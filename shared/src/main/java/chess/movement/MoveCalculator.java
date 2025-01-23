package chess.movement;
import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;

import java.util.HashSet;


interface MoveCalculator {

    static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        return null;
    }
    static boolean validMove(ChessPosition position){
        int row = position.getRow();
        int col = position.getColumn();
        //implement collision logic

        return (row < 8 && row >= 0 && col >=0 && col < 8);

    }
}
