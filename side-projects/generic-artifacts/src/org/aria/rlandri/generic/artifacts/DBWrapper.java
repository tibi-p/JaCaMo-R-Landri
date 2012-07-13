package org.aria.rlandri.generic.artifacts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Class that implements the update on the user's rank and economy
 * 
 * @author Eduard Tutescu
 */
public class DBWrapper {

	private static final String DB_REL_PATH = "rlandri\\db\\rlandri.sql";

	private Statement stat;
	private Connection conn;
	private String databasePath = DB_REL_PATH;

	public DBWrapper() throws ClassNotFoundException {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("config.properties"));
			String djangoDirectory = prop.getProperty("django_directory");
			File databaseFile = new File(djangoDirectory, DB_REL_PATH);
			databasePath = databaseFile.getAbsolutePath();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Class.forName("org.sqlite.JDBC");
	}

	private void createConnection() throws Exception {
		conn = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
		stat = conn.createStatement();
		conn.setAutoCommit(true);
	}

	private void closeConnection() throws SQLException {
		conn.close();
	}

	public int getRank(int userId) throws Exception {
		createConnection();

		// Get the data for the userId provided as argument
		ResultSet rs = stat
				.executeQuery("select * from envuser_envuser where id = "
						+ userId + ";");
		int rank = rs.getInt("rank");
		closeConnection();
		return rank;
	}

	public int getEconomy(int userId) throws Exception {
		createConnection();

		// Get the data for the userId provided as argument
		ResultSet rs = stat
				.executeQuery("select * from envuser_envuser where id = "
						+ userId + ";");
		int economy = rs.getInt("economy");

		closeConnection();
		return economy;
	}

	public void updateRank(int userId, int rank) throws Exception {
		// Update the observable rank property
		createConnection();
		stat.executeUpdate("update envuser_envuser set rank = " + rank
				+ " where id = " + userId + ";");

		closeConnection();
	}

	public void updateEconomy(int userId, int economy) throws Exception {
		// Update the observable economy property
		createConnection();
		stat.executeUpdate("update envuser_envuser set economy = " + economy
				+ " where id = " + userId + ";");

		closeConnection();
	}

}
