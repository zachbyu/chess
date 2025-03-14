package chess.movement;
import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;

import java.util.HashSet;


interface MoveCalculator {

    static boolean inBounds(ChessPosition position){
        int row = position.getRow();
        int col = position.getColumn();

        return (row <= 8 && row > 0 && col > 0 && col <= 8);

    }
    static void extracted(ChessBoard board,
                          ChessPosition myPosition,
                          int[][] changes,
                          int currentRow,
                          int currentCol,
                          ChessPiece currentPiece,
                          HashSet<ChessMove> moves,
                          boolean infiniteMovement) {
        for (int[] item : changes){
            int newRow = currentRow;
            int newCol = currentCol;
            do {
                newRow = newRow + item[0];
                newCol = newCol + item[1];
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                if (!MoveCalculator.inBounds(newPosition)) {
                    break;
                }
                //implement collision logic
                if(board.getPiece(newPosition) != null){
                    if(board.getPiece(newPosition).getTeamColor() == currentPiece.getTeamColor()){
                        break;
                    }else{
                        moves.add(new ChessMove(myPosition, newPosition, null));
                        break;
                    }
                }
                moves.add(new ChessMove(myPosition, newPosition, null));
            }while(infiniteMovement);
        }
    }

}
