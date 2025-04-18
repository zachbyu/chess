package server;
import com.google.gson.Gson;
import dataaccess.*;
import handlers.*;
import model.AuthData;
import server.websocket.WebSocketHandler;
import service.*;
import spark.*;
import server.websocket.WebSocketHandler;


public class Server {

    private final UserService userService;
    private final AuthService authService;
    private final GameService gameService;

    private final AuthDAO authDataAccess;
    private final WebSocketHandler webSocketHandler;


    public Server(){
        try{
            DatabaseManager.createDatabase();
        }catch(DataAccessException ex){
            throw new RuntimeException(ex);
        }

        GameDAO gameDataAccess = new SQLGameDAO();
        UserDAO userDataAccess = new SQLUserDAO();
        this.authDataAccess = new SQLAuthDAO();
        this.userService = new UserService(userDataAccess,authDataAccess);
        this.authService = new AuthService(userDataAccess,authDataAccess);
        this.gameService = new GameService(userDataAccess, authDataAccess, gameDataAccess);
        webSocketHandler = new WebSocketHandler();

    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
//        Spark.delete("/db", this::clear);
        Spark.webSocket("/ws", webSocketHandler);
        Spark.post("/user", this::register);
        Spark.delete("/db", this::clear);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.post("/game", this::createGame);
        Spark.get("/game", this::listGames);
        Spark.put("/game", this::joinGame);


        Spark.exception(DataAccessException.class, this::exceptionHandler);
        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }


    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void exceptionHandler(DataAccessException ex, Request req, Response res) {
        res.status(ex.getErrorType());
        ExceptionMessageResult message = new ExceptionMessageResult(ex.getMessage());
        String jsonResponse = new Gson().toJson(message);
        res.body(jsonResponse);
    }



    private Object register(Request request, Response response) throws DataAccessException {
        RegisterRequest registerRequest = new Gson().fromJson(request.body(), RegisterRequest.class);
        RegisterResult registerResult = userService.register(registerRequest);
        return new Gson().toJson(registerResult);
    }

    private Object clear(Request request, Response response) throws DataAccessException{
        userService.clear();
        authService.clear();
        gameService.clearGames();
        return "";
    }

    private Object login(Request request, Response response) throws DataAccessException {
        LoginRequest loginRequest = new Gson().fromJson(request.body(), LoginRequest.class);
        LoginResult loginResult = userService.login(loginRequest);
        return new Gson().toJson(loginResult);
    }

    private Object logout(Request request, Response response)throws DataAccessException{
        String authToken = request.headers("authorization");
        if(validateToken(authToken)) {
            userService.logout(authToken);
        }
        return "";
    }

    private Object createGame(Request request, Response response)throws DataAccessException{
        String authToken = request.headers("authorization");
        if (validateToken(authToken)){
            CreateGameRequest createGameRequest = new Gson().fromJson(request.body(), CreateGameRequest.class);
            CreateGameResult createGameResult = gameService.createGame(createGameRequest);
            return new Gson().toJson(createGameResult);
        }else{
            throw new DataAccessException(401, "Error: unauthorized");
        }
    }

    private Object listGames(Request request, Response response)throws DataAccessException{
        String authToken = request.headers("authorization");
        validateToken(authToken);
        ListGamesResult listGamesResult = gameService.listGames();
        return new Gson().toJson(listGamesResult);

    }

    private Object joinGame(Request request, Response response)throws DataAccessException{
        String authToken = request.headers("authorization");
        validateToken(authToken);
        String username = getUsername(authToken);
        JoinGameRequest joinGameRequest = new Gson().fromJson(request.body(), JoinGameRequest.class);
        gameService.joinGame(joinGameRequest, username);
        return "";
    }

    private boolean validateToken(String authToken)throws DataAccessException{
        try{
            AuthData data = authDataAccess.getAuth(authToken);
            if (data.authToken() == null || authToken.isEmpty()){
                throw new DataAccessException(401, "Error: unauthorized");
            }else{
                return true;
            }
        } catch (Exception e) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
    }

    private String getUsername(String authToken)throws DataAccessException{
        AuthData auth = authDataAccess.getAuth(authToken);
        return auth.username();
    }
}
