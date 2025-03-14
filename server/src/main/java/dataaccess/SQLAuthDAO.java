package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO{

    public SQLAuthDAO() {
        try { DatabaseManager.createDatabase(); } catch (DataAccessException ex) {
            throw new RuntimeException(ex);
        }
        try (var conn = DatabaseManager.getConnection()) {
            var createTestTable = """            
                    CREATE TABLE if NOT EXISTS auth (
                                    username VARCHAR(255) NOT NULL,
                                    authToken VARCHAR(255) NOT NULL,
                                    PRIMARY KEY (authToken)
                                    )""";
            try (var createTableStatement = conn.prepareStatement(createTestTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT username, authToken FROM auth WHERE authToken=?")) {
                statement.setString(1, authToken);
                var results = statement.executeQuery();
                    if (results.next()){
                    var username = results.getString("username");
                    return new AuthData(authToken, username);
                }
            }
        } catch (SQLException e){
            throw new DataAccessException("Auth token " + authToken + " does not exist");
        }
        return null;
    }

    @Override
    public AuthData createAuth(AuthData authData) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()){
            try(var statement = conn.prepareStatement("INSERT INTO auth (username, authToken) VALUES(?, ?)")){
                statement.setString(1, authData.username());
                statement.setString(2, authData.authToken());
                statement.executeUpdate();
                return authData;
            }
        }catch(SQLException | DataAccessException e){}
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try (var statement = conn.prepareStatement("DELETE FROM auth WHERE authToken=?")){
                statement.setString(1,authToken);
                statement.executeUpdate();
            }
        }catch (SQLException | DataAccessException e){}
    }

    @Override
    public void clearAuths() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()){
            try(var statement = conn.prepareStatement("TRUNCATE auth")){
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException | DataAccessException e){}
        }
}
