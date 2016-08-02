package com.depths.untold;

import com.depths.untold.Buildings.Building;
import com.depths.untold.Buildings.BuildingType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class UntoldPlayer {
    
    private final UUID uuid;
    private final Untold plugin;
    private int experience = 0;
    
    private Map<BuildingType, Integer> quotas = new HashMap<BuildingType, Integer>();
    
    public UntoldPlayer(Player p) {
        this.plugin = (Untold) Bukkit.getServer().getPluginManager().getPlugin("Untold");
        this.uuid = p.getUniqueId();
        for (BuildingType b : BuildingType.values()) {
            quotas.put(b, 1);
        }
    }
    
    public void addExperience(int exp) {
        experience += exp;
    }
    
    public int getExperience() {
        return experience;
    }
    
    private int getBuildingCount(BuildingType bt) {
        int count = 0;
        for (Building b : plugin.getBuildingManager().getBuildings()) {
            if (bt == b.type && b.owner.equals(this.uuid)) {
                count ++;
            }
        }
        return count;
    }
    
    public boolean hitQuotaLimit(BuildingType b) {
        return quotas.get(b) - getBuildingCount(b) <= 0;
    }
    
    
    
//    public boolean load() {
//        Connection con = MySQL.getConnection();
//        try {
//            Statement st = con.createStatement();
//            ResultSet rs = st.executeQuery("SELECT * FROM `players` WHERE uuid='"+uuid+"';");
//            if (rs.first()){ // valid row is found
//                
//            }
//            return true;
//        } catch (SQLException ex) {
//            Logger.getLogger(UntoldPlayer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return false;
//    }
//    
//    
//    public static void initializeTable () {
//        try {
//            Statement st = MySQL.getConnection().createStatement();
//            st.executeUpdate("CREATE TABLE IF NOT EXISTS players (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(100) NOT NULL, password VARCHAR(255), PRIMARY KEY (id));");
//        }catch (Exception ex) {
//            Bukkit.getConsoleSender().sendMessage(ex.getMessage());
//        }
//    }
}
