package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;

import java.util.UUID;


public class AuthService {

    private final AuthDAO authDataAccess;

    public AuthService(UserDAO userDataAccess, AuthDAO authDataAccess) {
        this.authDataAccess = authDataAccess;
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public void clear() throws DataAccessException{
        authDataAccess.clearAuths();
    }
}