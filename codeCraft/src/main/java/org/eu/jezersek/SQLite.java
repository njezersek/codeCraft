package org.eu.jezersek;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLite extends Database{
    String dbname;
    public SQLite(App instance){
        super(instance);
        dbname = plugin.getConfig().getString("SQLite.Filename", "robots"); // Set the table name here e.g player_kills
    }

    public String SQLiteCreateTokensTable = "CREATE TABLE IF NOT EXISTS robots (" +
            "`id` varchar(32) NOT NULL," + 
            "`inventory` BLOB NOT NULL," +
            "PRIMARY KEY (`id`)" + 
            ");";

    public String SQLiteCreateProgramsTable = "CREATE TABLE IF NOT EXISTS programs (" +
    "`id` varchar(36) NOT NULL," + 
    "`xml` BLOB NOT NULL," +
    "`js` BLOB NOT NULL," +
    "PRIMARY KEY (`id`)" + 
    ");";


    // SQL creation stuff, You can leave the blow stuff untouched.
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbname+".db");
        dataFolder.getParentFile().mkdirs();
        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: "+dbname+".db");
            }
        }
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTokensTable);
            s.executeUpdate(SQLiteCreateProgramsTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}