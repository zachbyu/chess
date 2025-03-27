package ui;

import model.GameData;
import server.Server;
import server.handlers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ChessClient {
    private static ServerFacade facade;
    private static final Scanner scan = new Scanner(System.in);
    private static String currAuthToken = null;
    private static ArrayList<GameData> lastGameList;
    private static State state = State.LOGGEDOUT;

    public ChessClient(int port){
        facade = new ServerFacade(port);
    }

    public void run() {;
        System.out.println("Welcome to 240 Chess: type help to begin.");
        var response = "";
        while (!response.equals("quit")){
            printState();
            String line = scan.nextLine();
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
        }
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

    private String register(String[] params) throws Exception{
        if (params.length == 3){
            RegisterRequest request = new RegisterRequest(params[0], params[1], params[2]);
            RegisterResult result = facade.register(request);
            currAuthToken = result.authToken();
            state = State.LOGGEDIN;
            return ("Registered as " + result.username());
        }
        throw new Exception("Expected <Username> <password> <email>");
    }

    private String login(String[] params) throws Exception{
        if (params.length == 2){
            LoginRequest request = new LoginRequest(params[0], params[1]);
            LoginResult result = facade.login(request);
            state = State.LOGGEDIN;
            currAuthToken = result.authToken();
            return ("Logged in as " + result.username());
        }
        throw new Exception("Expected <username> <password>");
    }

    private String logout(String[] params)throws Exception{
        if (params.length == 0){
            checkSignedIn();
//            System.out.println(currAuthToken);
            facade.logout(currAuthToken);
            currAuthToken = null;
            state = State.LOGGEDOUT;
            return ("logged out successfully");
        }
        throw new Exception("Failed to logout.");
    }

    private String createGame(String[] params)throws Exception{
        checkSignedIn();
        if (params.length == 1){
            CreateGameRequest request = new CreateGameRequest(params[0]);
            CreateGameResult result = facade.createGame(request);
            return("Game "+ params[0] + " created with ID " + result.gameID());
        }
        throw new Exception("Expected <game name>");
    }

    private void checkSignedIn() throws Exception{
        if(state == State.LOGGEDOUT){
            throw new Exception("Must be Signed in.");
        }
    }
}
