package chess.movement;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.HashSet;

public class BishopMoves implements MoveCalculator{
    public static HashSet<ChessMove> getBishopMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        int currentRow = myPosition.getRow();
        int currentCol = myPosition.getColumn();
        int[][] changes = new int[][] {{1,-1}, {1,1}, {-1, 1}, {-1, -1}};
        for (int[] item : changes){
            int newRow = currentRow;
            int newCol = currentCol;
            while(true) {
                newRow = newRow + item[0];
                newCol = newCol + item[1];
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                if (MoveCalculator.validMove(newPosition)) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }else{
                    break;
                }
            }
        }



        return moves;
            //make new rows and columns
    }

}
