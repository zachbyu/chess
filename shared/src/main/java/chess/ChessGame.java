package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    public ChessGame() {
        setTeamTurn(TeamColor.WHITE);
        board.resetBoard();
        gameOver = false;

    }
    private TeamColor teamTurn;
    private ChessBoard board = new ChessBoard();
    private boolean gameOver;
    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    public void setGameOver(boolean status){
        this.gameOver = status;
    }

    public boolean isGameOver(){
        return gameOver;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
//        ChessBoard testboard = board.clone();
        // could not figure out how to call isincheck with a cloned board
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null){
            return null;
        }
        Collection<ChessMove> allmoves = piece.pieceMoves(board, startPosition);
        HashSet<ChessMove> actualMoves = new HashSet<>();
        for (ChessMove move : allmoves){
            //test move
            ChessPosition end = move.getEndPosition();
            ChessPiece capturedPiece = board.getPiece(end);
            board.addPiece(end, piece);
            board.addPiece(startPosition, null);
            if (!isInCheck(piece.getTeamColor())){
                actualMoves.add(move);
            }
            //reset board
            board.addPiece(end, capturedPiece);
            board.addPiece(startPosition, piece);
        }
        return actualMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);
        TeamColor currentTurnColor = getTeamTurn();
        //no piece
        if (piece == null){
            throw new InvalidMoveException("no piece at start position");
        }
        //wrong turn
        if (piece.getTeamColor() != currentTurnColor){
            throw new InvalidMoveException("it is not this piece team turn");
        }
        //default to cannot make a move
        boolean valid = false;
        ChessPiece.PieceType promo = move.getPromotionPiece();
        Collection<ChessMove> allPossibleMoves = validMoves(start);
        for (ChessMove possibleMove : allPossibleMoves){
            if (possibleMove.equals(move)) {
                //this move is possible
                valid = true;
                break;
            }
        }
        //move not in validMoves
        if (!valid){
            throw new InvalidMoveException("not a valid move");
        }
        else{
            //move piece
            if (promo != null){
                board.addPiece(end, new ChessPiece(currentTurnColor, promo));
            }else {
                board.addPiece(end, piece);
            }
            board.addPiece(start, null);
            //change team turn
            if (getTeamTurn() == TeamColor.WHITE){
                setTeamTurn(TeamColor.BLACK);
            }else{
                setTeamTurn(TeamColor.WHITE);
            }
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        //get opposing team color, so I can iterate through those pieces and check if the King is in the possible moves
        TeamColor oppTeamColor = TeamColor.WHITE;
        if (teamColor == TeamColor.WHITE){
            oppTeamColor = TeamColor.BLACK;
        }
        //need to know king position
        ChessPosition kingpos = null;
        for (int row = 1; row <= 8; row ++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition myPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(myPosition);
                if (piece == null || piece.getTeamColor() == oppTeamColor){
                    continue;
                }
                if (piece.getPieceType() == ChessPiece.PieceType.KING){
                    kingpos = myPosition;
                    break;
                }
            }
        }
        //cycle through movement of all pieces to see if kingpos is in that pieces possible moves
        for (int row = 1; row <= 8; row ++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition myPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(myPosition);
                if (piece == null || piece.getTeamColor() != oppTeamColor){
                    continue;
                }
                Collection<ChessMove> moves = piece.pieceMoves(board, myPosition);
                for (ChessMove move : moves){
                    ChessPosition end = move.getEndPosition();
                    if (end.equals(kingpos)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)){
            return false;
        }
        else {return teamValidMoves(teamColor);}
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)){
            return false;
        } else {return teamValidMoves(teamColor);}
    }

    public boolean teamValidMoves(TeamColor teamColor){
        for (int row = 1; row <= 8; row ++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition myPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(myPosition);
                if (piece == null || piece.getTeamColor() != teamColor){
                    continue;
                }
                Collection<ChessMove> moves = validMoves(myPosition);
                if (!moves.isEmpty()){
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}
