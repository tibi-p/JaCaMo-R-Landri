import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;
import java.sql.*;

/**
 *      Artifact that implements the update on the user's rank and economy
 */
public class DBWrapper extends Artifact {
    private ResultSet rs;
    private Statement stat;
    private Connection conn;
    private String databasePath = "C:\\Users\\Tibi\\Desktop\\R'Landri\\JaCaMo-R-Landri\\rlandri\\db\\rlandri.sql";
    
    private void createConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        stat = conn.createStatement();
        conn.setAutoCommit(true);
    }
    
    private void closeConnection() throws Exception {
        conn.close();
    }
    
    @OPERATION public int getRank(int userId)  throws Exception {
        createConnection();
        
        // Get the data for the userId provided as argument
        ResultSet rs = stat.executeQuery("select * from envuser_envuser where id = " + userId + ";");
        int rank = rs.getInt("rank");
        closeConnection();
        return rank;
    }
    
    @OPERATION public int getEconomy(int userId)  throws Exception {
        createConnection();
        
        // Get the data for the userId provided as argument
        ResultSet rs = stat.executeQuery("select * from envuser_envuser where id = " + userId + ";");
        int economy = rs.getInt("economy");
        
        closeConnection();
        return economy;
    }
    
    @OPERATION public void updateRank(int userId, int rank) throws Exception {
        // Update the observable rank property
        createConnection();
        stat.executeUpdate("update envuser_envuser set rank = " + 
                           rank + 
                           " where id = " + 
                           userId +
                           ";");
        
        closeConnection();
    }

    @OPERATION public void updateEconomy(int userId, int economy) throws Exception {
        // Update the observable economy property
        createConnection();
        stat.executeUpdate("update envuser_envuser set economy = " + 
                           economy + 
                           " where id = " + 
                           userId +
                           ";");

        closeConnection();
    }
    
}

