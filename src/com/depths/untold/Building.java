/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.depths.untold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author Tim
 */
public class Building {
    public Buildings.BuildingType type;
    public int id; // Primary key
    private UUID owner;
    public List<UUID> subowners = new ArrayList<UUID>();
    public List<Vector> corners;
    transient private Map<String, ArrayList<Location>> borders = new HashMap<String, ArrayList<Location>>(); // cache for visible building boarders


    public Building (int id, UUID owner, List<Vector> corners, Buildings.BuildingType type) {
        this.corners = corners;
        this.id = id;
        this.owner = owner;
        this.type = type;
    }

    /**
     * Creating with an unknown primary key (new entry)
     */
    public Building (UUID owner, Vector pos, int size, Buildings.BuildingType type) {
        corners = new ArrayList();
        this.id = 0;
        this.owner = owner;
        this.type = type;
        corners.add(new Vector(pos.getX()-size, 0, pos.getZ()-size));
        corners.add(new Vector(pos.getX()+size, 0, pos.getZ()-size));
        corners.add(new Vector(pos.getX()+size, 0, pos.getZ()+size));
        corners.add(new Vector(pos.getX()-size, 0, pos.getZ()+size));
    }
    
    public boolean hasMember(Player p) {
        return hasMember(p.getUniqueId());
    }
    
    public boolean hasMember(UUID uuid) {
        if (owner.equals(uuid))
            return true;
        for (UUID _uuid : subowners) {
            if (_uuid.equals(uuid))
                return true;
        }
        return false;
    }
    
    public UUID getOwner() {
        return this.owner;
    }
    
    public List<UUID> getMembers() {
        List<UUID> members = new ArrayList<UUID>();
        members.addAll(subowners);
//        members.add(owner);
        return members;
    }
    
    public void addMember(UUID uuid) {
        String _uuid = uuid.toString();
        for (UUID m : subowners) {
           if (m.toString().equals(_uuid) ) {
               return;
           }
        }
        subowners.add(uuid);
    }

    public void clearBorders(Player p) {
        String uuid = p.getUniqueId().toString();
        if (borders.containsKey(uuid)) {
            ArrayList<Location> locs = borders.get(uuid);
            for (Location loc : locs) {
                p.sendBlockChange(loc, loc.getBlock().getType(), (byte) 0);
            }
            borders.remove(uuid);
        }
    }

    public void showBorder(Player p) {
        String uuid = p.getUniqueId().toString();
        clearBorders(p);
        borders.put(uuid, new ArrayList());
        ArrayList<Location> locs = borders.get(uuid);
        for (Vector v: corners) {
            Location loc = v.toLocation(p.getWorld());
            loc.setY(p.getLocation().getY()+5);
            while (loc.getBlock().isEmpty()) {
                loc.subtract(0, 1, 0);
            }
            locs.add(loc);
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
            if (pos.getZ() >= corners.get(0).getZ() - criteria && // within region vertically with criteria
                    pos.getZ() <= corners.get(2).getZ() + criteria ) {
                return false;
            }
        }
        return true;
    }
}
