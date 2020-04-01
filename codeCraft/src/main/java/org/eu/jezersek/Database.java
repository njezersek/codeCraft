package org.eu.jezersek;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.inventory.Inventory;


public abstract class Database {
    App plugin;
    Connection connection;
    // The name of the table we created back in SQLite class.
    public String table = "robots";
    public int tokens = 0;
    public Database(App instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize(){
        connection = getSQLConnection();
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table + " WHERE id = ?");
            ps.setString(1, "test");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);
   
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    }

    // metode za pisanje in branje iz baze
    public void setInvetory(String id, Inventory inventory){
        Connection conn = null;
        PreparedStatement ps = null;
        
        String base64inventory = InventorySerializator.inventoryToBase64(inventory);

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT OR REPLACE INTO "+table+" (id, inventory) VALUES (?, ?)");
            ps.setString(1, id);
            ps.setString(2, base64inventory);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public Inventory getInventory(String id, String title){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM "+table+" WHERE id = ?");
            ps.setString(1, id);

            rs = ps.executeQuery();

            if(rs.next()){
                return InventorySerializator.inventoryFromBase64(rs.getString("inventory"), new TmpInventoryHolder(id), title);
            }
        }catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }
    
    public void removeInventory(String id){
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM "+table+" WHERE id = ?");
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }
}
