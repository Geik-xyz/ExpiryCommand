package leaderos.web.tr.timed.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import leaderos.web.tr.timed.utils.TimeObject;

public class DatabaseQueries {
	
	public static void createTable() {
		
        try (Connection connection = ConnectionPool.getConnection();
             Statement statement = connection.createStatement()) {
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Tablo`\n" +
                    "(\n" +
                    " `id` INTEGER PRIMARY KEY AUTOINCREMENT ,\n" +
                    " `username`         varchar(20) NOT NULL ,\n" +
                    " `command`          varchar(30) NOT NULL ,\n" +
                    " `creationTime`          long DEFAULT 0 ,\n" +
                    " `expireTime`          long DEFAULT 0\n" +
                    ");");
            statement.executeBatch();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
	
	public static List<TimeObject> getExpiredPlayers() throws SQLException, ClassNotFoundException {
		String SQL_QUERY = "SELECT * FROM Tablo WHERE ? > expireTime";
        List<TimeObject> valueTotal = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement(SQL_QUERY);
            long unix = System.currentTimeMillis() / 1000L;

            pst.setLong(1, unix);

            ResultSet resultSet = pst.executeQuery();
            
            while (resultSet.next()) {
            	String name = resultSet.getString("username");
            	String product = resultSet.getString("command");
            	TimeObject chestObj = new TimeObject(name, product, unix);
            	valueTotal.add(chestObj);
            }
            resultSet.close();
            pst.close();
        }
        return valueTotal;
	}
	public static List<TimeObject> getPlayerValues(String username) throws SQLException, ClassNotFoundException {
		String SQL_QUERY = "SELECT * FROM Tablo WHERE username = ?";
        List<TimeObject> valueTotal = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement(SQL_QUERY);

            pst.setString(1, username);

            ResultSet resultSet = pst.executeQuery();
            
            while (resultSet.next()) {
            	String name = resultSet.getString("username");
            	String product = resultSet.getString("command");
            	long now = System.currentTimeMillis() / 1000L;
            	long expire = resultSet.getLong("expireTime");
            	long unix = expire - now;
            	TimeObject chestObj = new TimeObject(name, product, unix);
            	valueTotal.add(chestObj);
            }
            resultSet.close();
            pst.close();
        }
        return valueTotal;
	}
	public static boolean playerHasTimedValue(String username, String value) throws SQLException, ClassNotFoundException {
		String SQL_QUERY = "SELECT command FROM Tablo WHERE username = ? AND command = ?";
        boolean resultBool = false;
        try (Connection con = ConnectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement(SQL_QUERY);

            pst.setString(1, username);
            pst.setString(2, value);

            ResultSet resultSet = pst.executeQuery();
            
            while (resultSet.next()) {
            	if (value.equalsIgnoreCase(resultSet.getString("command"))) {
            		resultBool = true;}
            }
            resultSet.close();
            pst.close();
        }
        return resultBool;
	}
	public static void removeProductOfPlayer(String player, String product) throws SQLException, ClassNotFoundException {
		String SQL_QUERY = "DELETE FROM Tablo WHERE username = ? AND command = ?";
        try (Connection con = ConnectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement(SQL_QUERY);
            
            pst.setString(1, player);
            pst.setString(2, product);

            pst.executeUpdate();
            pst.close();
        }
	}
	public static void removeExpiredPersons() throws SQLException, ClassNotFoundException {
		String SQL_QUERY = "DELETE FROM Tablo WHERE ? > expireTime";
        try (Connection con = ConnectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement(SQL_QUERY);
            long unix = System.currentTimeMillis() / 1000L;

            pst.setLong(1, unix);

            pst.executeUpdate();
            pst.close();
        }
	}
	public static int setCommandInterval(String name, String product, int expireday) throws SQLException, ClassNotFoundException {
		String SQL_QUERY = "INSERT INTO Tablo (username, command, creationTime, expireTime) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement(SQL_QUERY);
            
            long now = System.currentTimeMillis() / 1000L;
            long expire = now + (expireday*86400);

            pst.setString(1, name);
            pst.setString(2, product);
            pst.setString(3, String.valueOf(now));
            pst.setString(4, String.valueOf(expire));

            int returnValue = pst.executeUpdate();
        	
            pst.close();
            return returnValue;
        }
	}
	public static int migrationCommandInterval(String name, String product, long startTime, long expireday) throws SQLException, ClassNotFoundException {
		String SQL_QUERY = "INSERT INTO Tablo (username, command, creationTime, expireTime) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement(SQL_QUERY);

            pst.setString(1, name);
            pst.setString(2, product);
            pst.setString(3, String.valueOf(startTime));
            pst.setString(4, String.valueOf(expireday));

            int returnValue = pst.executeUpdate();
        	
            pst.close();
            return returnValue;
        }
	}
	public static int accumulateTime(String name, String product, int day) throws SQLException, ClassNotFoundException {
		String SQL_QUERY = "UPDATE Tablo SET expireTime = ? WHERE username = ? AND command = ?";
        try (Connection con = ConnectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement(SQL_QUERY);
            
            long now = System.currentTimeMillis() / 1000L;
            long expire = now + (day*86400);
            long oldExpire = getProductExpireTimeOfPlayer(name, product);
            long lastExpire = oldExpire+(expire - now);

            pst.setLong(1, lastExpire);
            pst.setString(2, name);
            pst.setString(3, product);

            int returnValue = pst.executeUpdate();
        	
            pst.close();
            return returnValue;
        }
	}
	public static long getProductExpireTimeOfPlayer(String username, String product) throws SQLException, ClassNotFoundException {
		String SQL_QUERY = "SELECT expireTime FROM Tablo WHERE username = ? AND command = ?";
        long valueTotal = 0;
        try (Connection con = ConnectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement(SQL_QUERY);

            pst.setString(1, username);
            pst.setString(2, product);

            ResultSet resultSet = pst.executeQuery();
            
            if (resultSet.next()) {
            	valueTotal = resultSet.getLong("expireTime");
            }
            resultSet.close();
            pst.close();
        }
        return valueTotal;
	}

}
