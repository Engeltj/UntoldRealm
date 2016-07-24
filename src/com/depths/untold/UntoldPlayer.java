package com.depths.untold;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

/**
 *
 * @author Tim
 */
public class UntoldPlayer {
    private final UUID uuid;
    
    public UntoldPlayer(UUID uuid) {
        this.uuid = uuid;
    }
    
    
    public boolean load() {
        Connection con = MySQL.getConnection();
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `players` WHERE uuid='"+uuid+"';");
            if (rs.first()){ // valid row is found
                
            }
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(UntoldPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    
    public static void initializeTable () {
        try {
            Statement st = MySQL.getConnection().createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS players (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(100) NOT NULL, password VARCHAR(255), PRIMARY KEY (id));");
        }catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(ex.getMessage());
        }
    }
}
