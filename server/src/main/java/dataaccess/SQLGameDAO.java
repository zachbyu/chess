package dataaccess;

import java.sql.SQLException;

public class SQLGameDAO implements GameDAO{

    public SQLGameDAO() {
        try { DatabaseManager.createDatabase(); } catch (DataAccessException ex) {
            throw new RuntimeException(ex);
        }
        try (var conn = DatabaseManager.getConnection()) {
            var createTestTable = """            
                    CREATE TABLE if NOT EXISTS game (
                                    gameID INT NOT NULL,
                                    whiteUsername VARCHAR(255),
                                    blackUsername VARCHAR(255),
                                    gameName VARCHAR(255),
                                    chessGame TEXT,
                                    PRIMARY KEY (gameID)
                                    )""";
            try (var createTableStatement = conn.prepareStatement(createTestTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


}
