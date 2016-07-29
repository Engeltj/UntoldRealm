/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import org.bukkit.util.Vector;

/**
 *
 * @author Tim
 */
public class Buildings {
    public class Building {
        public int id; // Primary key
        public UUID owner;
        public Vector pos;
        public int size;
        public BuildingType type;
        
        public Building (int id, UUID owner, Vector pos, int size, BuildingType type) {
            this.id = id;
            this.owner = owner;
            this.pos = pos;
            this.size = size;
            this.type = type;
        }
        
        /**
         * Creating with an unknown primary key (new entry)
         */
        public Building (UUID owner, Vector pos, int size, BuildingType type) {
            this.id = 0;
            this.owner = owner;
            this.pos = pos;
            this.size = size;
            this.type = type;
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
        Connection conn = MySQL.getConnection();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM buildings;");
            while (rs.next()) {
                int id = rs.getInt("id");
                UUID owner = UUID.fromString(rs.getString("uuid"));
                double x = rs.getDouble("x");
                double z = rs.getDouble("z");
                int size = rs.getInt("size");
                String type = rs.getString("type");
                
                Vector v = new Vector(x,0,z);
                
                buildings.add(new Building(id, owner, v, size, BuildingType.valueOf(type)));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Buildings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveAll() {
        
    }
    
    public boolean update(Building building) {
        return true;
    }
    
    public boolean create(UUID owner, Location location, BuildingType type) {
        location.setY(0);
        Building b = new Building(owner, location.toVector(), 3, type);
        buildings.add(b);
        return true;
    }
    
    /**
     * Checks if a location is far enough enough from buildings, uses the size
     * to determine
     * @param actor The user performing test
     * @param location Location test point
     * @param minDistance Distance required
     * @return 
     */
    public boolean hasClearence(UUID actor, Location location, int minDistance){
        location.setY(0);
        Vector v = location.toVector();
        for (Building b : buildings) {
            if (b.owner.compareTo(actor) != 0) {
                Vector v2 = b.pos;
                if (Math.abs(v.getX() - v2.getX()) < minDistance + b.size) {
                    if (Math.abs(v.getZ() - v2.getZ()) < minDistance + b.size) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public boolean hasCapital(UUID actor) {
        for (Building b : buildings) {
            if (b.owner.compareTo(actor) != 0) {
                if (b.type == BuildingType.CAPITAL)
                    return true;
            }
        }
        return false;
    }
}
