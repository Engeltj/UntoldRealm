package com.depths.untold;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author Tim
 */
public class Buildings {
    
    public enum BuildingType {
        TOWN, CAPITAL, ARENA, SHOP
    }
    
    private List<Building> buildings = new ArrayList<Building>();
    
    public List<Building> getBuildings() {
        return buildings;
    }
    
    public List<Building> getTownBuildings(Building b) {
        List<Building> _buildings = new ArrayList<Building>();
        if (b.type == BuildingType.TOWN) {
            List<UUID> members = b.getMembers();
            for (Building _b : buildings) {
                for (UUID uuid : members) {
                    if (_b != b && _b.hasMember(uuid))
                        _buildings.add(_b);
                }
            }
        }
        return _buildings;
    }
    
    public Building getBuildingFromCorner(Location loc) {
        Vector v = loc.toVector();
        v.setY(0);
        for (Building b : buildings) {
            for (Vector corner : b.corners) {
                if (v.equals(corner)) {
                    return b;
                }
            }
        }
        return null;
    }
    
    public Building getBuilding(Location loc) {
        Vector v = loc.toVector();
        v.setY(0);
        Building town = null;
        for (Building b : buildings) {
            if (!b.hasClearance(v, -1)) {
                if (b.type != BuildingType.TOWN) 
                    return b;
                else
                    town = b; //town last priority building selection.
            }
        }
        return town;
    }
    
    public void load () {
//        Connection conn = MySQL.getConnection();
//        try {
//            Statement st = conn.createStatement();
//            ResultSet rs = st.executeQuery("SELECT * FROM buildings;");
//            while (rs.next()) {
//                int id = rs.getInt("id");
//                UUID owner = UUID.fromString(rs.getString("uuid"));
//                double x = rs.getDouble("x");
//                double z = rs.getDouble("z");
//                int size = rs.getInt("size");
//                String type = rs.getString("type");
//                
//                Vector v = new Vector(x,0,z);
//                
//                buildings.add(new Building(id, owner, v, size, BuildingType.valueOf(type)));
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(Buildings.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    public boolean update(Building building) {
        return true;
    }
    
    public Building create(Player owner, Location location, BuildingType type) {
        Vector v = location.toVector();
        v.setY(0);
        Building b = new Building(owner.getUniqueId(), v, 2, type);
        buildings.add(b);
        return b;
    }
    
    public void destroy(Building b) {
        buildings.remove(b);
    }
    
    public boolean canBuild(Player p, Location location) {
        Vector v = location.toVector();
        v.setY(0);
        for (Building b : buildings) {
            if (!b.hasMember(p) && !b.hasClearance(v, 0)) {
                return false;
            }
        }
        return true;
    }
    
    public String canCreateBuilding(Player p, Location location, BuildingType bt) {
        Vector v = location.toVector();
        v.setY(0);
        Building town = getTown(p);
        if (town != null) {
            if (!town.hasClearance(location.toVector(), -3)) { // within town
                for (Building b : buildings) { // Checks not overlapping a current building
                    if (b.type != BuildingType.TOWN && !b.hasClearance(v, 1)) {
                        b.showBorder(p);
                        return "Too close to another building";
                    }
                }
                return null;
            } else if (town.hasClearance(location.toVector(), 0)) {
                return "Building needs to be inside a settlement.";
            } else {
                return "Too close to settlement borders";
            }
        } else {
            if (bt == BuildingType.TOWN) {
                return null;
            } else {
                return "You need to build a settlement first";
            }
        }
    }
    
    public Building getTown(Player p) {
        for (Building b : buildings) {
            if (b.type == BuildingType.TOWN && b.hasMember(p)) {
                return b;
            }
        }
        return null;
    }
    
    public boolean hasTown(Player p) {
        return (getTown(p)!=null);
    }
}
