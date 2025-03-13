package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class SQLUserDAO implements UserDAO{

    public SQLUserDAO() {
        try { DatabaseManager.createDatabase(); } catch (DataAccessException ex) {
            throw new RuntimeException(ex);
        }
        try (var conn = DatabaseManager.getConnection()) {
            var createTestTable = """            
                    CREATE TABLE if NOT EXISTS user (
                                    username VARCHAR(255) NOT NULL,
                                    password VARCHAR(255) NOT NULL,
                                    email VARCHAR(255),
                                    PRIMARY KEY (username)
                                    )""";
            try (var createTableStatement = conn.prepareStatement(createTestTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()){
            try(var statment = conn.prepareStatement("INSERT INTO user (username, password, email) VALUES (?, ?, ?)")){
                statment.setString(1, user.username());
                statment.setString(2, hashPassword(user.password()));
                statment.setString(3, user.email());
                statment.executeUpdate();
            }
        }catch(SQLException | DataAccessException e){}
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try(var statement = conn.prepareStatement("SELECT username, password, email FROM user WHERE username=?")){
                statement.setString(1, username);
                try (var results = statement.executeQuery()){
                    results.next();
                    var pass = results.getString("password");
                    var email = results.getString("email");
                    return new UserData(username, pass, email);
                }
            }
        }catch(SQLException e){
            throw new DataAccessException("User " + username + " does not exist");
        }
    }

    @Override
    public void clearUsers() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()){
            try(var statement = conn.prepareStatement("TRUNCATE user")){
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }catch (SQLException | DataAccessException e){}
    }

    public String hashPassword(String pass){
        String hashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt());
        return hashedPassword;
    }
}
