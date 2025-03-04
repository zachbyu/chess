package server;
import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import server.handlers.*;
import service.*;
import spark.*;

public class Server {

    private final UserService userService;
    private final AuthService authService;

    private final UserDAO userDataAccess;
    private final AuthDAO authDataAccess;


    public Server(){
        this.userDataAccess = new MemoryUserDAO();
        this.authDataAccess = new MemoryAuthDAO();
        this.userService = new UserService(userDataAccess,authDataAccess);
        this.authService = new AuthService(userDataAccess,authDataAccess);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
//        Spark.delete("/db", this::clear);
        Spark.post("/user", this::register);
        Spark.delete("/db", this::clear);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);


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
            authDataAccess.deleteAuth(authToken);
        }
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
}
