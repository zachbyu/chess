package chess.movement;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashSet;

public class KingMoves implements MoveCalculator{
    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        ChessPiece currentPiece = board.getPiece(myPosition);
        int currentRow = myPosition.getRow();
        int currentCol = myPosition.getColumn();
        int[][] changes = new int[][] {{1,-1}, {1,1}, {-1, 1}, {-1, -1}, {0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        MoveCalculator.extracted(board, myPosition, changes, currentRow, currentCol, currentPiece, moves, false);




        return moves;
    }
}
