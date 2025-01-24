package chess.movement;

import chess.*;

import java.util.HashSet;

public class PawnMoves implements MoveCalculator {
    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        ChessPiece currentPiece = board.getPiece(myPosition);
        int currentRow = myPosition.getRow();
        int currentCol = myPosition.getColumn();
        // front movement check
        //White
        if (currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE && currentRow == 2){
            int newRow = currentRow + 1;
            ChessPosition newPosition = new ChessPosition(newRow, currentCol);
            if(board.getPiece(newPosition) == null) {
                moves.add(new ChessMove(myPosition, newPosition, null));
                newPosition = new ChessPosition(newRow + 1, currentCol);
                if (board.getPiece(newPosition) == null){
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }else if(currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE){
            int newRow = currentRow + 1;
            ChessPosition newPosition = new ChessPosition(newRow, currentCol);
            if (newRow == 8){
                if (board.getPiece(newPosition) == null){
                    moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.BISHOP));
                }
            }
            else {
                if (MoveCalculator.inBounds(newPosition)) {
                    if (board.getPiece(newPosition) == null) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }

        //Black
        if (currentPiece.getTeamColor() == ChessGame.TeamColor.BLACK && currentRow == 7){
            int newRow = currentRow - 1;
            ChessPosition newPosition = new ChessPosition(newRow, currentCol);
            if(board.getPiece(newPosition) == null) {
                moves.add(new ChessMove(myPosition, newPosition, null));
                newPosition = new ChessPosition(newRow - 1, currentCol);
                if (board.getPiece(newPosition) == null){
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }else if(currentPiece.getTeamColor() == ChessGame.TeamColor.BLACK){
            int newRow = currentRow - 1;
            ChessPosition newPosition = new ChessPosition(newRow, currentCol);
            if (newRow == 1){
                if (board.getPiece(newPosition) == null){
                    moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.BISHOP));
                }
            }
            else {
                if (MoveCalculator.inBounds(newPosition)) {
                    if (board.getPiece(newPosition) == null) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }

        //Capture Logic
        //White
        if (currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE){
            int [][] changes = {{1, 1}, {1, -1}};
            for (int[] item : changes) {
                int newRow = currentRow + item[0];
                int newCol = currentCol + item[1];
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                if (MoveCalculator.inBounds(newPosition)) {
                    //check if piece there
                    if(board.getPiece(newPosition) != null && board.getPiece(newPosition).getTeamColor() != currentPiece.getTeamColor()) {
                        if (newRow == 8){
                            moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.KNIGHT));
                            moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.BISHOP));
                        }else{
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                    }
                }
            }
        }

        //Black Capture
        if (currentPiece.getTeamColor() == ChessGame.TeamColor.BLACK){
            int [][] changes = {{-1, 1}, {-1, -1}};
            for (int[] item : changes) {
                int newRow = currentRow + item[0];
                int newCol = currentCol + item[1];
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                if (MoveCalculator.inBounds(newPosition)) {
                    //check if piece there
                    if(board.getPiece(newPosition) != null && board.getPiece(newPosition).getTeamColor() != currentPiece.getTeamColor()) {
                        if (newRow == 1){
                            moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.KNIGHT));
                            moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.BISHOP));
                        }else{
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                    }
                }
            }
        }


        return moves;
    }
}