package dataaccess;

import model.UserData;

public interface UserDAO {
    UserData getUser(String username) throws DataAccessException;
    UserData createUser(UserData user) throws DataAccessException;
    void clearUsers() throws DataAccessException;

}
