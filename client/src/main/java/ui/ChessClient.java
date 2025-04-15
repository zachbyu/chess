package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import handlers.*;
import model.GameData;
import ui.websocket.ServerMessageObserver;
import ui.websocket.WebSocketFacade;
import websocket.messages.*;

import java.util.*;

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
                    GameData currGame = gameMap.get(id);
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
                    GameData currentGame = gameMap.get(id);
                    try {
                        facade.joinGame(new JoinGameRequest(white ? "WHITE" : "BLACK", currentGame.gameID()));
                        CreateBoard observeBoard = new CreateBoard(currentGame.game().getBoard(), white, null);
                        observeBoard.drawBoard();
                        return ("Now joining the game " + currentGame.gameName() + " as " + (white ? "WHITE" : "BLACK"));
                    } catch (Exception e) {
                        return ("Could not join game, Assure spot is available");
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
        drawCurrentBoard(currGame.gameID(), white, null);
        return "board redrawn";
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
        System.out.print(SET_TEXT_COLOR_BLUE);
        System.out.println(notificationMessage.getMessage());
    }
    private void displayError(String message){
        ErrorMessage errorMessage = gson.fromJson(message, ErrorMessage.class);
        System.out.print(SET_TEXT_COLOR_RED);
        System.out.println(errorMessage.getErrorMessage());
        System.out.print(SET_TEXT_COLOR_GREEN);
    }
    private void loadGame(String message){
        LoadGameMessage loadGameMessage = gson.fromJson(message, LoadGameMessage.class);
        currGame = new GameData(currGame.gameID(), currGame.whiteUsername(), currGame.blackUsername(), currGame.gameName(), loadGameMessage.getGame());
        drawChessGame(white, null, loadGameMessage.getGame());
        System.out.println();
    }


}
