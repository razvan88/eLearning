package database;

import static database.DBCredentials.DRIVER;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * This class will store a connection to a database
 * 
 * @author Razvan Nedelcu
 *
 */
public class DBConnection {
	private Connection mConnection;
	private String mDatabaseName;
	
	public DBConnection(String databaseName) {
		this.mDatabaseName = databaseName;
	}
	
	/**
	 * This method is the first one that should be called,
	 * for opening the connection
	 */
	public void openConnection() {
        try {
			Class.forName(DRIVER).newInstance();
			String link = DBCredentials.getLink(mDatabaseName);
            mConnection = DriverManager.getConnection(link);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * Closes the connection with the database.
	 * This is the last function that should be called.
	 */
    public void closeConnection() {
        try {
            mConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @return the sql Connection object
     * wrapped in this class
     */
    public Connection getConnection() {
    	return mConnection;
    }
}
