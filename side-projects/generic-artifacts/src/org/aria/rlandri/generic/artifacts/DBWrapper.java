package org.aria.rlandri.generic.artifacts;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class that implements the update on the user's rank and economy
 * 
 * @author Eduard Tutescu
 */
public class DBWrapper implements Closeable {

	private static final String DATABASE_URL = "//localhost:3306/";
	private static final String DATABASE_USER = "root";
	private static final String DATABASE_PASSWORD = "";

	private final Connection conn;

	public DBWrapper() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:mysql:" + DATABASE_URL,
				DATABASE_USER, DATABASE_PASSWORD);
		conn.setAutoCommit(true);
	}

	public int getRank(int userId) throws Exception {
		Statement stat = conn.createStatement();

		// Get the data for the userId provided as argument
		ResultSet rs = stat
				.executeQuery("select * from envuser_envuser where id = "
						+ userId + ";");
		int rank = rs.getInt("rank");
		return rank;
	}

	public int getEconomy(int userId) throws Exception {
		Statement stat = conn.createStatement();

		// Get the data for the userId provided as argument
		ResultSet rs = stat
				.executeQuery("select * from envuser_envuser where id = "
						+ userId + ";");
		int economy = rs.getInt("economy");
		return economy;
	}

	public void updateRank(int userId, int rank) throws Exception {
		// Update the observable rank property
		Statement stat = conn.createStatement();
		stat.executeUpdate("update envuser_envuser set rank = " + rank
				+ " where id = " + userId + ";");
	}

	public void updateEconomy(int userId, int economy) throws Exception {
		// Update the observable economy property
		Statement stat = conn.createStatement();
		stat.executeUpdate("update envuser_envuser set economy = " + economy
				+ " where id = " + userId + ";");
	}

	public void close() throws IOException {
		try {
			conn.close();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

}
