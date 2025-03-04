package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import server.handlers.RegisterRequest;
import server.handlers.*;

public class UserService {

    private final UserDAO userDataAccess;
    private final AuthDAO authDataAccess;

    public UserService(UserDAO userDataAccess, AuthDAO authDataAccess){
        this.userDataAccess = userDataAccess;
        this.authDataAccess = authDataAccess;
    }

    public RegisterResult register(RegisterRequest registerRequest)throws DataAccessException {
        String username = registerRequest.username();
        String pass = registerRequest.password();
        String email = registerRequest.email();
        if (username == null || pass == null || email == null){
            throw new DataAccessException(400, "Error: bad request");
        }
        UserData user = userDataAccess.getUser(username);
        if (user != null){
            throw new DataAccessException(403, "Error: user taken");
        }
        else{
            UserData newuser = new UserData(username, pass, email);
            userDataAccess.createUser(newuser);
            String authToken = AuthService.generateToken();
            AuthData auth = new AuthData(authToken, username);
            authDataAccess.createAuth(auth);
            RegisterResult result = new RegisterResult(username, authToken);
            return result;
        }
    }

    public void clear() throws DataAccessException{
        userDataAccess.clearUsers();
    }
}
