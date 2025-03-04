package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO{
    private HashMap<String, AuthData> auths = new HashMap<>();

    public AuthData createAuth(AuthData auth) throws DataAccessException{
        auths.put(auth.authToken(), auth);
        return auth;
    }
    public AuthData getAuth(String authToken)throws DataAccessException{
        return auths.get(authToken);
    }

    public void deleteAuth(String authToken) throws DataAccessException{
        auths.remove(authToken);
    }

    public void clearAuths(){
        auths.clear();
    }
}
