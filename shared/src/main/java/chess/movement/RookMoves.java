package chess.movement;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashSet;

public class RookMoves implements MoveCalculator{
    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        ChessPiece currentPiece = board.getPiece(myPosition);
        int currentRow = myPosition.getRow();
        int currentCol = myPosition.getColumn();
//        boolean infinteMoves = true;
        int[][] changes = new int[][] {{0,-1}, {0,1}, {-1, 0}, {1, 0}};
        MoveCalculator.extracted(board, myPosition, changes, currentRow, currentCol, currentPiece, moves, true);




        return moves;
    }
}
