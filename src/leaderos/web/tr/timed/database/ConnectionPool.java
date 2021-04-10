package leaderos.web.tr.timed.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import leaderos.web.tr.timed.Main;

public class ConnectionPool {
	
	
	public static Connection conn = null;
	
	public static Connection getConnection() throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		File databaseFile = new File("plugins/ExpiryCommand/Database.db");
        return DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getParentFile().getAbsolutePath() + "/Database.db");
    }

    public static void closePool() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
	
	public static void initsqlite() {
		File databaseFile = new File("plugins/ExpiryCommand/Database.db");
		Bukkit.getConsoleSender().sendMessage(Main.color("&bLeader&fOS&c ExpiryCommand &6Developed by Geik"));
		if (!databaseFile.exists()) {
	    	try {
	    		Class.forName("org.sqlite.JDBC");
	    		String url = "jdbc:sqlite:" + databaseFile.getParentFile().getAbsolutePath() + "/Database.db";
	    		conn = DriverManager.getConnection(url);
	    		
	    	} catch(SQLException | ClassNotFoundException e) {
	    		e.printStackTrace();
	    	} finally {
	    		try {
	    			if (conn != null) {
	    				conn.close();
	    			}
	    		} catch(SQLException ex) {
	    			ex.printStackTrace();
	    		}
	    	}
		}
    }

}
