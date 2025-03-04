package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception{

    private final int errorType;
    public DataAccessException(int errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    public int getErrorType(){
        return errorType;
    }
}
