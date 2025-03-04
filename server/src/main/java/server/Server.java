package server;
import com.google.gson.Gson;
import dataaccess.*;
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

    private void exceptionHandler(DataAccessException exception, Request request, Response response){
        response.status(exception.getErrorType());
        ExceptionMessageResult errorMessage = new ExceptionMessageResult(exception.getMessage());
        response.body(new Gson().toJson(errorMessage));
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
}
