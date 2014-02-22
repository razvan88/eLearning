package database;

import java.util.HashMap;

/**
 * This class will store the database connections, corresponding
 * to each school
 * 
 * @author Razvan Nedelcu
 *
 */
public class DBConnectionManager { 
	private static HashMap<Integer, DBConnection> sConnections;
	private static Object sLock;	
	
	static {
		sConnections = new HashMap<Integer, DBConnection>();
		sLock = new Object();
	}
	
	private DBConnectionManager() {}
	
	public static DBConnection getConnection(int schoolId, String databaseName) {
		synchronized (sLock) {
			DBConnection connection = null;
			
			if(!sConnections.containsKey(schoolId)) {
				connection = new DBConnection(databaseName);
				connection.openConnection();
				sConnections.put(schoolId, connection);
			} else {
				connection = sConnections.get(schoolId);
			}
			
			return connection;
		}
	}
	
	public static boolean addConnection(int schoolId, DBConnection connection) {
		synchronized (sLock) {
			if(!sConnections.containsKey(schoolId)) {
				sConnections.put(schoolId, connection);
				return true;
			}
			return false;
		}
	}
	
}
