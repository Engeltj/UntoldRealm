package com.depths.untold;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author Tim
 */
public class Buildings {
    public class Building {
        public BuildingType type;
        public int id; // Primary key
        public UUID owner;
        public List<Vector> corners;
        
        public Building (int id, UUID owner, Vector pos, int size, BuildingType type) {
            corners = new ArrayList();
            this.id = id;
            this.owner = owner;
            this.type = type;
            corners.add(new Vector(pos.getX()-size, 0, pos.getZ()-size));
            corners.add(new Vector(pos.getX()+size, 0, pos.getZ()-size));
            corners.add(new Vector(pos.getX()+size, 0, pos.getZ()+size));
            corners.add(new Vector(pos.getX()-size, 0, pos.getZ()+size));
        }
        
        /**
         * Creating with an unknown primary key (new entry)
         */
        public Building (UUID owner, Vector pos, int size, BuildingType type) {
            corners = new ArrayList();
            this.id = 0;
            this.owner = owner;
            this.type = type;
            corners.add(new Vector(pos.getX()-size, 0, pos.getZ()-size));
            corners.add(new Vector(pos.getX()+size, 0, pos.getZ()-size));
            corners.add(new Vector(pos.getX()+size, 0, pos.getZ()+size));
            corners.add(new Vector(pos.getX()-size, 0, pos.getZ()+size));
        }
        
        public void showBorder(Player p) {
            for (Vector v: corners) {
                Location loc = v.toLocation(p.getWorld());
                loc.setY(p.getLocation().getY());
                p.sendBlockChange(loc, Material.GLOWSTONE, (byte) 0);
            }
            
        }
        
        /**
         * Get distance of this building from a given position
         * @param pos
         * @param criteria
         * @return 
         */
        public boolean hasClearance(Vector pos, int criteria) {
            if (pos.getX() < corners.get(0).getX()) { // To the left of region
                if (corners.get(0).getX() - pos.getX() <= criteria) { // fails region distance criteria horizontally
                    if (pos.getZ() >= corners.get(0).getZ() - criteria && // within region vertically with criteria
                            pos.getZ() <= corners.get(2).getZ() + criteria ) {
                        return false;
                    }
                }
            } else if (pos.getX() > corners.get(1).getX()) { // To the right of region
                if (pos.getX() - corners.get(1).getX() <= criteria) { // fails region  distance criteria horizontally
                    if (pos.getZ() >= corners.get(0).getZ() - criteria && // within region vertically with criteria
                            pos.getZ() <= corners.get(2).getZ() + criteria ) {
                        return false;
                    }
                }
            } else { // within region horizontally
                Bukkit.broadcastMessage(pos.getZ() + ">=" + corners.get(0).getZ());
                if (pos.getZ() >= corners.get(0).getZ() - criteria && // within region vertically with criteria
                        pos.getZ() <= corners.get(2).getZ() + criteria ) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public enum BuildingType {
        CAPITAL, WALL, ARENA, SHOP
    }
    
    private List<Building> buildings = new ArrayList<Building>();
    
    public List<Building> getBuildings() {
        return buildings;
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
        Building b = new Building(owner.getUniqueId(), v, 3, type);
        buildings.add(b);
        return b;
    }
    
    public boolean canBuild(Player p, Location location) {
        Vector v = location.toVector();
        v.setY(0);
        for (Building b : buildings) {
            if (!b.owner.equals(p.getUniqueId()) && !b.hasClearance(v, 0)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean canCreateBuilding(Player p, Location location, BuildingType bt) {
        Vector v = location.toVector();
        v.setY(0);
        for (Building b : buildings) { // Checks not overlapping a current building
            if (!b.hasClearance(v, 5)) {
                b.showBorder(p);
                return false;
            }
        }
        if (bt != BuildingType.CAPITAL) {
            for (Building b : buildings) { // Checks in a 20 block radius of another building owned by player
                if (b.owner.equals(p.getUniqueId()) && !b.hasClearance(v, 25)) {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }
    
    public boolean hasCapital(Player actor) {
        for (Building b : buildings) {
            if (b.owner.compareTo(actor.getUniqueId()) != 0) {
                if (b.type == BuildingType.CAPITAL)
                    return true;
            }
        }
        return false;
    }
}
