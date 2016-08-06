package com.depths.untold;

import com.depths.untold.Buildings.BuildingType;
import static org.depths.untold.generated.Tables.PLAYERS;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jooq.*;
import org.jooq.impl.*;


/**
 *
 * @author Tim
 */
public class UntoldPlayer {
    
    private final UUID uuid;
    private final Untold plugin;
    private int experience = 0;
    
    transient private Vector move_building;
    
    private final Map<BuildingType, Integer> quotas = new HashMap<BuildingType, Integer>();
    
    public UntoldPlayer(Player p) {
        this.plugin = (Untold) Bukkit.getServer().getPluginManager().getPlugin("Untold");
        this.uuid = p.getUniqueId();
        for (BuildingType b : BuildingType.values()) {
            quotas.put(b, 1);
        }
        load();
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
            if (bt == b.type && b.hasMember(uuid)) {
                count ++;
            }
        }
        return count;
    }
    
    public boolean hitQuotaLimit(BuildingType b) {
        return quotas.get(b) - getBuildingCount(b) <= 0;
    }
    
    public Building modifyBuilding(Player p, Building b, Location loc) {
        Vector v = loc.toVector();
        v.setY(0);
        
        if (move_building == null) {
            move_building = v;
        } else {
            final Player _p = p;
            final Building _b = plugin.getBuildingManager().getBuildingFromCorner(new Location(loc.getWorld(), move_building.getX(), move_building.getY(), move_building.getZ()));
            int idx = _b.corners.indexOf(move_building);
            if (idx == 0) {
                _b.corners.get(0).setX(loc.getX());
                _b.corners.get(0).setZ(loc.getZ());
                _b.corners.get(1).setZ(loc.getZ());
                _b.corners.get(3).setX(loc.getX());
            } else if (idx == 1) {
                _b.corners.get(1).setX(loc.getX());
                _b.corners.get(1).setZ(loc.getZ());
                _b.corners.get(0).setZ(loc.getZ());
                _b.corners.get(2).setX(loc.getX());
            } else if (idx == 2) {
                _b.corners.get(2).setX(loc.getX());
                _b.corners.get(2).setZ(loc.getZ());
                _b.corners.get(1).setX(loc.getX());
                _b.corners.get(3).setZ(loc.getZ());
            } else if (idx == 3) {
                _b.corners.get(3).setX(loc.getX());
                _b.corners.get(3).setZ(loc.getZ());
                _b.corners.get(0).setX(loc.getX());
                _b.corners.get(2).setZ(loc.getZ());
            }
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    _b.showBorder(_p);
                }
            }, 5);
            move_building = null;
            return _b;
        }
        return null;
    }
    
    public boolean isModifyingBuilding() {
        return move_building != null;
    }
    
    private void load() {
        DSLContext db = DSL.using(MySQL.getConnection(), SQLDialect.MYSQL);
        Record r = db.select().from(PLAYERS).where(PLAYERS.UUID.equal(uuid.toString())).fetchOne();
        if (r == null) {
            Bukkit.broadcastMessage("Player not found, creating for " + uuid.toString());
            db.insertInto(PLAYERS, PLAYERS.UUID).values(uuid.toString());
        }
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
