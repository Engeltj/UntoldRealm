/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.depths.untold;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import static org.depths.untold.generated.Tables.BUILDINGS;
import static org.depths.untold.generated.Tables.BUILDING_COORDS;
import static org.depths.untold.generated.Tables.BUILDING_MEMBERS;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

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
    
    public String name = "";
    public String welcome_msg = "";


    public Building (int id, UUID owner, List<Vector> corners, Buildings.BuildingType type) {
        this.corners = corners;
        this.id = id;
        this.owner = owner;
        this.type = type;
        this.welcome_msg = "Welcome to this region that is of type: " + type.name();
    }

    /**
     * Creating with an unknown primary key (new entry)
     * @param owner
     * @param pos
     * @param size
     * @param type
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
        this.welcome_msg = "Welcome to this region that is of type: " + StringUtils.capitalize(type.name());
    }
    
//    public boolean isLeaving(Player p) {
//        int w = corners.get(1).getBlockX() - corners.get(0).getBlockX();
//        int h = corners.get(2).getBlockZ() - corners.get(0).getBlockZ();
//        int size = Math.max(w, h);
//        Location loc = p.getLocation();
//        
//        if (Math.abs(loc.getBlockX() - corners.get(0).getBlockX()) < size+10) { // if potentially near region
//            if (loc.getBlockX() < corners.get(0).getBlockX()) { // left edge
//                
//            } else if (loc.getBlockZ() < corners.get(0).getBlockZ()) { // top edge
//            
//            } else if (loc.getBlockX() > corners.get(0).getBlockX()) { // right edge
//            
//            } else if (loc.getBlockZ() > corners.get(0).getBlockZ()) { // bottom edge
//            
//            }
//        } else {
//            return false;
//        }
//        return false;
//    }
    
    public boolean isInRegion(Player p) {
        if (!this.hasClearance(p.getLocation().toVector(), 1)) {
            //if (this.hasClearance(p.getLocation().toVector(), -3))
                return true;
        }
        return false;
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
    
    public void save() {
        DSLContext db = DSL.using(MySQL.getConnection(), SQLDialect.MYSQL);
        if (id == 0) { // create new building
            db.insertInto(BUILDINGS, BUILDINGS.OWNER, BUILDINGS.TYPE).values(getOwner().toString(), type.name()).execute();
            Record br = db.select().from(BUILDINGS)
                    .where(BUILDINGS.OWNER.equal(getOwner().toString()).and(BUILDINGS.TYPE.equal(type.name())))
                    .orderBy(BUILDINGS.ID.desc()).fetchAny();
            id = br.get(BUILDINGS.ID);
            
        }
        
        db.deleteFrom(BUILDING_COORDS).where(BUILDING_COORDS.BUILDING_ID.equal(id)).execute();
        db.deleteFrom(BUILDING_MEMBERS).where(BUILDING_MEMBERS.BUILDING_ID.equal(id)).execute();
        
        for (int i = 0; i< corners.size(); i++) {
            db.insertInto(BUILDING_COORDS, BUILDING_COORDS.BUILDING_ID, BUILDING_COORDS.CORNER_ID, BUILDING_COORDS.X, BUILDING_COORDS.Z)
                .values(id, i, corners.get(i).getBlockX(), corners.get(i).getBlockZ()).execute();
        }
        for (UUID uuid : getMembers()) {
            db.insertInto(BUILDING_MEMBERS, BUILDING_MEMBERS.BUILDING_ID, BUILDING_MEMBERS.UUID)
                .values(id, uuid.toString()).execute();
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
