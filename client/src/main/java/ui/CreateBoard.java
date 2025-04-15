package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static chess.ChessPiece.PieceType.*;
import static ui.EscapeSequences.*;

public class CreateBoard {
    private boolean whitePerspective;
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private chess.ChessBoard currentBoard;
    private int[][] highlights;

    public CreateBoard(chess.ChessBoard importedBoard, boolean whitePerspective, int[][] highlights){
        this.currentBoard = importedBoard;
        this.whitePerspective = whitePerspective;
        this.highlights = highlights;
    }

    public void drawBoard(){
        System.out.print(ERASE_SCREEN);
        System.out.println();
        drawHeaders(whitePerspective);
        drawChessBoard();
        drawHeaders(whitePerspective);
        System.out.print(SET_TEXT_COLOR_WHITE);

    }

    private void drawHeaders(boolean whitePerspective) {
        System.out.print(SET_BG_COLOR_LIGHT_GREY);
        System.out.print("   ");
        String[] headers = whitePerspective ? new String[]{"a", "b", "c", "d", "e", "f", "g", "h"}:
        new String[]{"h", "g", "f", "e", "d", "c", "b", "a"};
        for (int col = 0; col < BOARD_SIZE_IN_SQUARES; ++col){
            System.out.print(SET_TEXT_COLOR_BLACK);
            System.out.print(" ");
            System.out.print(headers[col]);
            System.out.print(" ");

        }
        System.out.print("   ");
        System.out.print(RESET_BG_COLOR);
        System.out.println();
    }

    private void drawChessBoard(){
        int startRow = whitePerspective ? 8 : 1;
        int endRow = whitePerspective ? 1 : 8;
        int rowStep = whitePerspective ? -1 : 1;

        int startCol = whitePerspective ? 1 : 8;
        int endCol = whitePerspective ? 8 : 1;
        int colStep = whitePerspective ? 1 : -1;

        for (int boardRow = startRow; (whitePerspective ? boardRow >= endRow : boardRow <=endRow); boardRow += rowStep){
            if (boardRow % 2 == 0){
                drawBlackRow(boardRow, startCol, endCol, colStep);
            }
            else{
                drawWhiteRow(boardRow, startCol, endCol, colStep);
            }
        }
    }

    private void drawBlackRow(int boardRow, int startCol, int endCol, int colStep){
        drawRowNumberSquare(boardRow);
        for (int boardCol = startCol; (whitePerspective ? boardCol <= endCol : boardCol >= endCol); boardCol += colStep) {
            if (boardCol % 2 == 0){
                drawBlackSquare(boardRow, boardCol);
            }else{
                drawWhiteSquare(boardRow, boardCol);
            }
        }
        drawRowNumberSquare(boardRow);
        System.out.print(RESET_BG_COLOR);
        System.out.println();
    }

    private void drawWhiteRow(int boardRow, int startCol, int endCol, int colStep){
        drawRowNumberSquare(boardRow);
        for (int boardCol = startCol; (whitePerspective ? boardCol <= endCol : boardCol >= endCol); boardCol += colStep) {
            if (boardCol % 2 == 0){
                drawWhiteSquare(boardRow, boardCol);
            }else{
                drawBlackSquare(boardRow, boardCol);
            }
        }
        drawRowNumberSquare(boardRow);
        System.out.print(RESET_BG_COLOR);
        System.out.println();
    }

    private void drawBlackSquare(int boardRow, int boardCol){
//        System.out.print(SET_BG_COLOR_DARK_GREEN);
//        drawPieces(boardRow, boardCol);
        drawSquare(boardRow, boardCol, SET_BG_COLOR_GREEN, SET_BG_COLOR_DARK_GREEN);
    }

    private void drawWhiteSquare(int boardRow, int boardCol) {
//        System.out.print(SET_BG_COLOR_WHITE);
//        drawPieces(boardRow, boardCol);
        drawSquare(boardRow, boardCol, SET_BG_COLOR_GREEN, SET_BG_COLOR_WHITE);
    }

    private void drawSquare(int boardRow, int boardCol, String setBgColorHighlight, String setBgColorNormal) {
        boolean highlighted = isHighlighted(boardRow, boardCol);
        if (highlighted) {
            System.out.print(setBgColorHighlight);
        } else {
            System.out.print(setBgColorNormal);
        }
        drawPieces(boardRow, boardCol);
    }

    private boolean isHighlighted(int boardRow, int boardCol) {
        boolean highlighted = false;
        if (highlights != null) {
            for (int i = 0; i < highlights.length; i++) {
                if (highlights[i][0] == boardRow && highlights[i][1] == boardCol) {
                    highlighted = true;
                    break;
                }
            }
        }
        return highlighted;
    }

    private static void drawRowNumberSquare(int boardRow) {
        System.out.print(SET_BG_COLOR_LIGHT_GREY);
        System.out.print(SET_TEXT_COLOR_BLACK);
        System.out.print(" " + boardRow + " ");
    }

    private void drawPieces(int boardRow, int boardCol){
        ChessPosition currentPosition = new ChessPosition(boardRow, boardCol);
        ChessPiece currentPiece = currentBoard.getPiece(currentPosition);
        if (currentPiece == null){
            System.out.print("   ");
        }
        else{
            if (currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                System.out.print(SET_TEXT_COLOR_RED);
            }else{System.out.print(SET_TEXT_COLOR_BLUE);}
            System.out.print(" ");
            drawPieceType(currentPiece);
            System.out.print(" ");
        }
    }

    private void drawPieceType(ChessPiece piece){
        if(piece.getPieceType() == PAWN){
            System.out.print("P");}
        else if(piece.getPieceType() == ROOK){
            System.out.print("R");}
        else if(piece.getPieceType() == KNIGHT){
            System.out.print("N");}
        else if(piece.getPieceType() == KING){
            System.out.print("K");}
        else if(piece.getPieceType() == QUEEN){
            System.out.print("Q");}
        else if(piece.getPieceType() == BISHOP){
            System.out.print("B");}
    }
}
