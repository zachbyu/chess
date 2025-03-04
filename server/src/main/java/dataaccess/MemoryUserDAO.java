package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO{
    private HashMap<String, UserData> users = new HashMap<>();

    public UserData getUser(String username){
        return users.get(username);
    }

    public UserData createUser(UserData user){
        users.put(user.username(), user);
        return user;
    }

    public void clearUsers(){
        users.clear();
    }
}
