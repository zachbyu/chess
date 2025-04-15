package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import handlers.*;
import model.GameData;
import ui.websocket.ServerMessageObserver;
import ui.websocket.WebSocketFacade;
import websocket.messages.*;

import java.util.*;

import static chess.ChessPiece.PieceType.*;
import static ui.EscapeSequences.*;

public class ChessClient implements ServerMessageObserver {
    private static ServerFacade facade;
    private WebSocketFacade websocket;
    private static final Scanner SCAN = new Scanner(System.in);
    private static String currAuthToken = null;
    private static ArrayList<GameData> lastGameList;
    private static State state = State.LOGGEDOUT;
    private HashMap<Integer, GameData> gameMap = new HashMap<>();
    private boolean white;
    private GameData currGame;
    private final String baseURL;
    public Gson gson = new Gson();

    public ChessClient(int port){
        facade = new ServerFacade(port);
        baseURL = "http://localhost:" + port;
    }

    public void run() {;
        System.out.println("Welcome to 240 Chess: type help to begin.");
        var response = "";
        while (!response.equals("quit")){
            printState();
            String line = SCAN.nextLine();
            try {
                response = this.evaluateLine(line);
                System.out.print(response);
            }catch(Throwable e){
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();

    }

    public void printState(){
        System.out.print("\n" + state + ">>> ");
    }

    public String evaluateLine(String input)throws Exception{
        var tokens = input.split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch (cmd){
            case "register" -> register(params);
            case "login" -> login(params);
            case "logout" -> logout(params);
            case "create" -> createGame(params);
            case "list" -> listGames(params);
            case "observe" -> observeGame(params);
            case "join" -> joinGame(params);
            case "redraw" -> redraw(params);
            case "move" -> makeMove(params);
            case "highlight" -> highlightMoves(params);
            case "resign" -> resign(params);
            case "leave" -> leaveGame(params);
            case "quit" -> "quit";
            default -> help();
        };
    }

    private String help() {
        if (state == State.LOGGEDOUT){
            return """
                    Available Commands:
                    To register a user: "register <username> <password> <email>"
                    To login: "login <username> <password>"
                    To quit chess: "quit"
                    To open the help menu: "help"
                    """;
        } else if (state == State.LOGGEDIN) {

            return """
                    Available Commands:
                    Create a game: "create <game name>"
                    List games: "list"
                    Observe an ongoing game: "observe <game ID>"
                    Join a game: "join <game ID> <WHITE/BLACK>"
                    To logout: "logout"
                    To quit chess: "quit"
                    open the help menu: "help"
                    """;
        }
        return """
                Available Commands:
                Redraw the game board: enter "redraw"
                Make move: Make a move on your turn, enter "move d2d4 -> Queen" the -> Piecetype is only needed for pawn promotion
                Highlight legal moves: enter "highlight d2"
                Resign: to resign the game enter "resign"
                Leave Game: enter "leave"
                to quit chess: "quit"
                open the help menu: "help"
                """;
    }

    private String register(String[] params) throws Exception{
        if (state.equals(State.LOGGEDIN)){
            return ("Already logged in, please logout to register another user");
        }
        if (params.length == 3){
            RegisterRequest request = new RegisterRequest(params[0], params[1], params[2]);
            try {
                RegisterResult result = facade.register(request);
                currAuthToken = result.authToken();
                state = State.LOGGEDIN;
                return ("Registered as " + result.username());
            } catch (Exception e) {
                return ("failed to register user, username is taken");
            }
        }
        return ("Expected <Username> <password> <email>");
    }

    private String login(String[] params) throws Exception{
        if (state.equals(State.LOGGEDIN)){
            return ("Already logged in, please logout to use another user");
        }
        if (params.length == 2){
            LoginRequest request = new LoginRequest(params[0], params[1]);
            try {
                LoginResult result = facade.login(request);
                state = State.LOGGEDIN;
                currAuthToken = result.authToken();
                return ("Logged in as " + result.username());
            } catch (Exception e) {
                return("failed to login user, please confirm your username and password");
            }
        }
        return ("Expected <username> <password>");
    }

    private String logout(String[] params)throws Exception{
        if (params.length == 0){
            try {
                checkSignedIn();
//            System.out.println(currAuthToken);
                facade.logout(currAuthToken);
                currAuthToken = null;
                state = State.LOGGEDOUT;
                return ("logged out successfully");
            } catch (Exception e) {
                return("failed to logout user");
            }
        }
        return ("Failed to logout.");
    }

    private String createGame(String[] params)throws Exception{
        checkSignedIn();
        if (params.length == 1){
            try {
                CreateGameRequest request = new CreateGameRequest(params[0]);
                CreateGameResult result = facade.createGame(request);
                return ("Game " + params[0] + " created");
            } catch (Exception e) {
                return("failed to create game");
            }
        }
        return ("Expected <game name>, one word names only");
    }

    private String listGames(String[] params)throws Exception{
        checkSignedIn();
        if (params.length == 0){
            try {
                ListGamesResult result = facade.listGames();
                ArrayList<GameData> games = result.games();
                int count = 1;
                for (GameData game : games) {
                    gameMap.put(count, game);
                    System.out.println(count+ ": " + game.gameName());
                    System.out.println("White Player " + game.whiteUsername() + ", Black Player " + game.blackUsername());
                    System.out.println();
                    count ++;
                }
                return ("All games listed");
            } catch (Exception e) {
                return("games could not be listed");
            }
        }else{
        return ("No games exist, feel free to create one!");}
    }

    private String observeGame(String[] params)throws Exception{
        checkSignedIn();
        if (params.length == 1) {
            try {
                int id = Integer.parseInt(params[0]);
                ListGamesResult listResult = facade.listGames();

                try{
                    currGame = gameMap.get(id);
//                    CreateBoard observeBoard = new CreateBoard(currGame.game().getBoard(), true);
//                    observeBoard.drawBoard();
                    websocket = new WebSocketFacade(baseURL, this);
                    websocket.observeGame(currAuthToken, currGame.gameID());
                    white = true;
                    state = State.INGAME;
                    return ("Now observing the game " + currGame.gameName());
                }catch(Exception e) {
                    return("not a valid id");
                }
            }
            catch (Exception e) {
                return ("Game does not exist");
            }
        }
        return ("expected format: <gameID>");
    }

    private String joinGame(String[] params) throws Exception{
        checkSignedIn();
        if (params.length == 2) {
            try {
                int id = Integer.parseInt(params[0]);
                if (Objects.equals(params[1], "WHITE")){
                     white = true;
                } else if (Objects.equals(params[1], "BLACK")) {
                    white = false;
                }else{return ("Expected: <GameID> <WHITE/BLACK>");}

                if (gameMap.containsKey(id)){
                    currGame = gameMap.get(id);
                    try {
                        facade.joinGame(new JoinGameRequest(white ? "WHITE" : "BLACK", currGame.gameID()));
                        state = State.INGAME;
                        websocket = new WebSocketFacade(baseURL, this);
                        websocket.joinGame(currAuthToken, currGame.gameID());
//                        CreateBoard observeBoard = new CreateBoard(currentGame.game().getBoard(), white, null);
//                        observeBoard.drawBoard();
                        return ("Now joining the game " + currGame.gameName() + " as " + (white ? "WHITE" : "BLACK"));
                    } catch (Exception e) {
                        return ("Could not join game, confirm spot is available");
                    }
                }else{
                    return ("Game not found, use list to see available games");
                }
            } catch (Exception e) {
                return ("expected format: <gameID> <WHITE/BLACK>");
            }
        }
        return ("expected format: <gameID> <WHITE/BLACK>");
    }

    private String redraw(String[] params) throws Exception{
        try{
            drawCurrentBoard(currGame.gameID(), white, null);
            return "board redrawn";
        }catch (Exception e){
            return "could not draw board";
        }
    }

    private String makeMove(String[] params)throws Exception{
        if(params.length == 1 || params.length == 3){
            String req = params[0];
            if(req.length()!=4){
                return "expected move in the format d2d4";
            }
            int startRow = getRow(req.substring(0,2));
            int startCol = getCol(req.substring(0,2));
            ChessPosition startPos = new ChessPosition(startRow, startCol);
            String start = req.substring(0,2);

            int endRow = getRow(req.substring(2,4));
            int endCol = getCol(req.substring(2,4));
            ChessPosition endPos = new ChessPosition(endRow, endCol);
            String end = req.substring(2,4);

            ChessMove move;
            if(params.length == 1){
                move = new ChessMove(startPos, endPos, null);
            }
            else{
                if(params[2].equals("Queen")){move = new ChessMove(startPos, endPos, QUEEN);}
                else if(params[2].equals("Knight")){move = new ChessMove(startPos, endPos, KNIGHT);}
                else if(params[2].equals("Rook")){move = new ChessMove(startPos, endPos, ROOK);}
                else if(params[2].equals("Bishop")){move = new ChessMove(startPos, endPos, BISHOP);}
                else{return "Please enter valid piece type";}
            }

            websocket.makeMove(currAuthToken, currGame.gameID(), move, start, end);
            drawCurrentBoard(currGame.gameID(), white, null);
            return ("move made");
        }
        return ("Expected: move <d7d8> -> <Queen>");
    }

    private String highlightMoves(String[] params) throws Exception {
        if(params.length == 1) {
            String req = params[0];

            int row = getRow(req);
            int col = getCol(req);

            ChessPosition pos = new ChessPosition(row,col);
            ChessGame chessGame = currGame.game();
            ChessBoard board = chessGame.getBoard();
            if (board.getPiece(pos) == null){
                return "No piece at position";
            }

            Collection<ChessMove> moves = chessGame.validMoves(pos);
            int numMoves = moves.size();
            int[][] positions = new int[numMoves][2];
            int i=0;

            for (ChessMove move: moves){
                ChessPosition potentialMove = move.getEndPosition();
                positions[i][0] = potentialMove.getRow();
                positions[i][1] = potentialMove.getColumn();

                if (!white) {
                    row = 9 - row;  // flip row: 1↔8, 2↔7, etc.
                    col = 9 - col;  // flip column: a↔h, b↔g, etc.
                }

                i++;
            }
            drawCurrentBoard(currGame.gameID(), white, positions);

            return ("You highlighted moves for " + req + "\n");
        }
        return("Expected form: highlight <a1>");
    }

    private String resign(String[] params) throws Exception {

        System.out.println("Are you sure you want to resign? This action is irreversible: Y/N:");

        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();

        if(line.equals("Y")){
            websocket.resign(currAuthToken, currGame.gameID());
            return "You have now resigned, the game is over.";
        }
        else if(line.equals("N")){
            return "Resignation cancelled, the game is still going.";
        }
        else{
            return "Please enter Y for yes or N for no";
        }
    }

    private String leaveGame(String[] params) throws Exception {
        websocket.leaveGame(currAuthToken, currGame.gameID());
        state = State.LOGGEDIN;
        return "game left successfully";
    }

    private void drawCurrentBoard(int id, boolean white, int[][] highlights) throws Exception {
        try{
            //GameData currGame = gameMap.get(id);
            getAndDrawBoard(currGame, white, highlights);
        }
        catch(Exception e){
            throw new Exception("Game ID not found. Type 'list' to get a list of current games");
        }
    }

    private static void getAndDrawBoard(GameData game, boolean white, int[][] highlights) {
        ChessGame currGame = game.game();
        drawChessGame(white, highlights, currGame);
    }

    private static void drawChessGame(boolean white, int[][] highlights, ChessGame currGame) {
        chess.ChessBoard chessClassBoard = currGame.getBoard();
        CreateBoard uiBoard = new CreateBoard(chessClassBoard, white, highlights);
        uiBoard.drawBoard();
    }

    private void checkSignedIn() throws Exception{
        if(state == State.LOGGEDOUT){
            throw new Exception("Must be Signed in.");
        }
    }

    @Override
    public void notify(String message) {

        ServerMessage serverMessage =
                gson.fromJson(message, ServerMessage.class);

        switch (serverMessage.getServerMessageType()) {
            case NOTIFICATION -> displayNotification(message);
            case ERROR -> displayError(message);
            case LOAD_GAME -> loadGame(message);
        }
    }
    private void displayNotification(String message){
        NotificationMessage notificationMessage = gson.fromJson(message, NotificationMessage.class);
        System.out.println(notificationMessage.getMessage());
    }
    private void displayError(String message){
        ErrorMessage errorMessage = gson.fromJson(message, ErrorMessage.class);
        System.out.println(errorMessage.getErrorMessage());
    }
    private void loadGame(String message){
        LoadGameMessage loadGameMessage = gson.fromJson(message, LoadGameMessage.class);
        currGame = new GameData(currGame.gameID(), currGame.whiteUsername(), currGame.blackUsername(), currGame.gameName(), loadGameMessage.getGame());
        drawChessGame(white, null, loadGameMessage.getGame());
        System.out.println();
    }


    private static int getRow(String req) throws Exception {
        int row;
        if (req.charAt(1) == '1'){row = 1;}
        else if (req.charAt(1) == '2') {row = 2;}
        else if (req.charAt(1) == '3') {row = 3;}
        else if (req.charAt(1) == '4') {row = 4;}
        else if (req.charAt(1) == '5') {row = 5;}
        else if (req.charAt(1) == '6') {row = 6;}
        else if (req.charAt(1) == '7') {row = 7;}
        else if (req.charAt(1) == '8') {row = 8;}
        else {
            throw new Exception("Please enter the square in form [abcdefgh][12345678]");
        }
        return row;
    }

    private static int getCol(String req) throws Exception {
        int col;
        if (req.charAt(0) == 'a'){col = 1;}
        else if (req.charAt(0) == 'b') {col = 2;}
        else if (req.charAt(0) == 'c') {col = 3;}
        else if (req.charAt(0) == 'd') {col = 4;}
        else if (req.charAt(0) == 'e') {col = 5;}
        else if (req.charAt(0) == 'f') {col = 6;}
        else if (req.charAt(0) == 'g') {col = 7;}
        else if (req.charAt(0) == 'h') {col = 8;}
        else {
            throw new Exception("Please enter the square in form [abcdefgh][12345678]");
        }
        return col;
    }


}
