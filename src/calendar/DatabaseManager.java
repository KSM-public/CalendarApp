package calendar;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseManager implements DataInterface {
	private String server, user, password;
	private int port;
	private ArrayList<String> dataArrayList;
	private ArrayList<String> settingsArrayList;
	
	public DatabaseManager(String server, String user, String password, int port) {
		this.server = server;
		this.user = user;
		this.password = password;
		this.port = port;
		
		dataArrayList = new ArrayList<String>();
		settingsArrayList = new ArrayList<String>();
	}
	
	public void setConnectionDetails(String server, String user, String password, int port) {
		this.server = server;
		this.user = user;
		this.password = password;
		this.port = port;
	}
	
	@Override
	public boolean openInterface() {
		String url = String.format("jdbc:sqlserver://%s:%s;database=Kalendarz;user=%s;password=%s;"
	            + "hostNameInCertificate=*.database.windows.net;loginTimeout=5;", server, ""+port, user, password);
		Connection connection = null;
	        
		try {
			connection = DriverManager.getConnection(url);
			connection.close();
	    }
		catch (Exception e) {
	        return false;
	    }
		
		return true;
	}
	
	//Erase all data in DB
	public boolean eraseAll() {
		String url = String.format("jdbc:sqlserver://%s:%s;database=Kalendarz;user=%s;password=%s;"
	            + "hostNameInCertificate=*.database.windows.net;loginTimeout=5;", server, ""+port, user, password);
		Connection connection = null;
	        
		try {
			connection = DriverManager.getConnection(url);
			
			
			Statement deleteEventsStatement = connection.createStatement();
			deleteEventsStatement.executeUpdate("TRUNCATE TABLE EventsTable");
			
			Statement deleteSettingsStatement = connection.createStatement();
			deleteSettingsStatement.executeUpdate("TRUNCATE TABLE SettingsTable");
			
			connection.close();
	    }
		catch (Exception e) {
	        return false;
	    }
	        
	    return true;
	}
	
	//Save data to DB in data table
	//DateTime expected format is: yyyy-MM-dd HH:mm
	public boolean addDataToDB(String[] data) {
		String url = String.format("jdbc:sqlserver://%s:%s;database=Kalendarz;user=%s;password=%s;"
	            + "hostNameInCertificate=*.database.windows.net;loginTimeout=5;", server, ""+port, user, password);
		Connection connection = null;
	        
		try {
			connection = DriverManager.getConnection(url);
			
			//6 values are needed
			if (data.length < 6)
				return false;
			
			String insertSql = "INSERT INTO EventsTable (event_description, event_location, event_active, event_startdatetime, event_enddatetime, event_notifyminutes)" +
			"VALUES (N'"+data[0]+"', N'"+data[1]+"', "+data[2]+", '"+data[3]+"', '"+data[4]+"', "+data[5]+")";
			
			Statement statement = connection.createStatement();
			statement.executeUpdate(insertSql);
			
			connection.close();
	    }
		catch (Exception e) {
	        return false;
	    }
	        
	    return true;
	}
	
	//Save settings in DB
	public boolean saveSettingsInDB(String[] data) {
		String url = String.format("jdbc:sqlserver://%s:%s;database=Kalendarz;user=%s;password=%s;"
	            + "hostNameInCertificate=*.database.windows.net;loginTimeout=5;", server, ""+port, user, password);
		Connection connection = null;
	        
		try {
			connection = DriverManager.getConnection(url);
			
			//3 values are needed
			if (data.length < 3)
				return false;
			
			String insertSql = "INSERT INTO SettingsTable(settings_clearoldperiod, settings_notifyperiod, settings_notifysound)" +
			"VALUES ("+data[0]+", "+data[1]+", "+data[2]+")";
			
			Statement deleteStatement = connection.createStatement();
			deleteStatement.executeUpdate("TRUNCATE TABLE SettingsTable");
			
			Statement setStatement = connection.createStatement();
			setStatement.executeUpdate(insertSql);
			
			connection.close();
	    }
		catch (Exception e) {
	        return false;
	    }
	        
	    return true;
	}
	
	//Remove data
	public boolean removeDataFromDB(String[] data) {
		String url = String.format("jdbc:sqlserver://%s:%s;database=Kalendarz;user=%s;password=%s;"
	            + "hostNameInCertificate=*.database.windows.net;loginTimeout=5;", server, ""+port, user, password);
		Connection connection = null;
	        
		try {
			connection = DriverManager.getConnection(url);
			
			//6 values are needed
			if (data.length < 6)
				return false;
			
			String deleteSql = "DELETE FROM EventsTable " +
			"WHERE event_description = N'"+data[0]+"' AND event_location = N'"+data[1]+"' AND event_active = "+data[2]+" AND event_startdatetime = '"+data[3]+"'"
					+ " AND event_enddatetime = '"+data[4]+"' AND event_notifyminutes = "+data[5]+"";
			
			Statement deleteStatement = connection.createStatement();
			deleteStatement.executeUpdate(deleteSql);
			
			connection.close();
	    }
		catch (Exception e) {
	        return false;
	    }
	        
	    return true;
	}
	
	//Modify data
	public boolean modifyDataInDB(String[] oldData, String[] newData) {
		String url = String.format("jdbc:sqlserver://%s:%s;database=Kalendarz;user=%s;password=%s;"
	            + "hostNameInCertificate=*.database.windows.net;loginTimeout=5;", server, ""+port, user, password);
		Connection connection = null;
	        
		try {
			connection = DriverManager.getConnection(url);
			
			//6 values are needed
			if (newData.length < 6 && oldData.length < 6)
				return false;
			
			String modifySql = "UPDATE EventsTable SET "
					+"event_description = N'"+newData[0]+"', event_location = N'"+newData[1]+"', event_active = "+newData[2]+", "
					+ "event_startdatetime = '"+newData[3]+"', event_enddatetime = '"+newData[4]+"', event_notifyminutes = "+newData[5]+" "
					+ "WHERE event_description = N'"+oldData[0]+"' AND event_location = N'"+oldData[1]+"' AND event_active = "+oldData[2]+" AND event_startdatetime = '"+oldData[3]+"'"
					+ " AND event_enddatetime = '"+oldData[4]+"' AND event_notifyminutes = "+oldData[5]+"";
			
			Statement deleteStatement = connection.createStatement();
			deleteStatement.executeUpdate(modifySql);
			
			connection.close();
	    }
		catch (Exception e) {
	        return false;
	    }
	        
	    return true;
	}
	
	//Get all data and save it in array
	public boolean getAllDataFromDB() {
		String url = String.format("jdbc:sqlserver://%s:%s;database=Kalendarz;user=%s;password=%s;"
	            + "hostNameInCertificate=*.database.windows.net;loginTimeout=5;", server, ""+port, user, password);
		Connection connection = null;
	        
		try {
			connection = DriverManager.getConnection(url);

			
			String selectSql = "SELECT event_description, event_location, event_active, event_startdatetime, event_enddatetime, event_notifyminutes FROM EventsTable";
			
			try (Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery(selectSql)) {

		          		while (resultSet.next()) {
		          			dataArrayList.add(resultSet.getString(1));
		          			dataArrayList.add(resultSet.getString(2));
		          			dataArrayList.add(resultSet.getString(3));
		          			dataArrayList.add(resultSet.getString(4));
		          			dataArrayList.add(resultSet.getString(5));
		          			dataArrayList.add(resultSet.getString(6));
		          		}
		          	
		                connection.close();
		            }
			
			connection.close();
	    }
		catch (Exception e) {
	        return false;
	    }
	        
	    return true;
	}
	
	//Get all settings
	public boolean getAllSettingsFromDB() {
		String url = String.format("jdbc:sqlserver://%s:%s;database=Kalendarz;user=%s;password=%s;"
	            + "hostNameInCertificate=*.database.windows.net;loginTimeout=5;", server, ""+port, user, password);
		Connection connection = null;
	        
		try {
			connection = DriverManager.getConnection(url);

			
			String selectSql = "SELECT settings_clearoldperiod, settings_notifyperiod, settings_notifysound FROM SettingsTable";
			
			try (Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery(selectSql)) {

		          		while (resultSet.next()) {
		          			settingsArrayList.add(resultSet.getString(1));
		          			settingsArrayList.add(resultSet.getString(2));
		          			settingsArrayList.add(resultSet.getString(3));
		          		}
		          	
		                connection.close();
		            }
			
			connection.close();
	    }
		catch (Exception e) {
	        return false;
	    }
	        
	    return true;
	}

	public ArrayList<String> getDataArrayList() {
		return dataArrayList;
	}


	public ArrayList<String> getSettingsArrayList() {
		return settingsArrayList;
	}
}
