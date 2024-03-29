package com.depths.untold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import static org.depths.untold.generated.Tables.*;
import org.depths.untold.generated.tables.records.BuildingsRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

/**
 *
 * @author Tim
 */
public class Buildings {
    
    public enum BuildingType {
        TOWN, 
        CAPITAL,
        ARENA, 
        SHOP, 
        HOUSE
    }
    
    public static final double TAX_PER_BLOCK = 0.05;
    public static final int COST_PER_BLOCK = 5;
    public static final int SHOP_INCOME = 200; // per day
    
    public final Map<BuildingType, String> DESCRIPTIONS;
   
    
    private List<Building> buildings = new ArrayList<Building>();
    private final Untold plugin;
    
    
    public Buildings() {
        plugin = (Untold) Bukkit.getServer().getPluginManager().getPlugin("Untold");
        DESCRIPTIONS = new HashMap<BuildingType, String>();
        DESCRIPTIONS.put(BuildingType.TOWN, "Main protection region, allows constructing buildings.");
        DESCRIPTIONS.put(BuildingType.CAPITAL, "Holds town statue, and becomes your respawn point.");
        DESCRIPTIONS.put(BuildingType.ARENA, "Fight to the death! Winner in the area accrues the losers town earnings for 24 hours.");
        DESCRIPTIONS.put(BuildingType.SHOP, "Place chests in this region fill with items, they immediately become for sale.");
        DESCRIPTIONS.put(BuildingType.HOUSE, "Private region, chests only accessible by you.");
    }
    
    public List<Building> getBuildings() {
        return buildings;
    }
    
    public List<Building> getTownBuildings(Building b) {
        List<Building> _buildings = new ArrayList<Building>();
        if (b.type == BuildingType.TOWN) {
            List<String> members = b.getMembers();
            members.add(b.getOwner());
            for (Building _b : buildings) {
                for (String uuid : members) {
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
    
    
    
    public Building getClosest(Player p) {
        Location loc = p.getLocation();
        Building closest_b = null;
        int closest_value = 999999999;
        for (Building b : buildings) {
            int dist = 999999999;
            if (loc.getBlockZ() >= b.corners.get(0).getBlockZ() &&
                    loc.getBlockZ() <= b.corners.get(3).getBlockZ()) {
                dist = Math.min(Math.abs(loc.getBlockX() - b.corners.get(0).getBlockX()),
                                  Math.abs(loc.getBlockX() - b.corners.get(1).getBlockX()));
            } else {
                dist = Math.min(Math.abs(loc.getBlockZ() - b.corners.get(0).getBlockZ()),
                                  Math.abs(loc.getBlockZ() - b.corners.get(1).getBlockZ()));
            }
            if (dist < closest_value) {
                closest_value = dist;
                closest_b = b;
            }
        }
        return closest_b;
    }
    
    public void load () {
        buildings.clear();
        DSLContext db = DSL.using(MySQL.getConnection(), SQLDialect.MYSQL);
        Record[] lr = db.select().from(BUILDINGS).fetchArray();
        for (Record r : lr) {
            int id = r.get(BUILDINGS.ID);
            BuildingType type = BuildingType.valueOf(r.get(BUILDINGS.TYPE));
            UUID owner = UUID.fromString(r.get(BUILDINGS.OWNER));
            
            Record[] r_corners = db.select().from(BUILDING_COORDS).where(BUILDING_COORDS.BUILDING_ID.equal(id)).orderBy(BUILDING_COORDS.CORNER_ID.asc()).fetchArray();
            List<Vector> corners = new ArrayList();
            for (Record c : r_corners) {
                int x = c.get(BUILDING_COORDS.X);
                int z = c.get(BUILDING_COORDS.Z);
                Vector v = new Vector(x, 0, z);
                corners.add(v);
            }
            
            Building b = new Building(id, owner, corners, type);
            b.name = r.get(BUILDINGS.NAME);
            b.welcome_msg = r.get(BUILDINGS.WELCOME_MSG);
            Record[] r_members = db.select().from(BUILDING_MEMBERS).where(BUILDING_MEMBERS.BUILDING_ID.equal(id)).fetchArray();
            for (Record c : r_members) {
                String uuid = c.get(BUILDING_MEMBERS.UUID);
                b.addMember(uuid);
            }
            
            buildings.add(b);
        }
    }
    
    public void save() {
        for (Building b : buildings) {
            b.save();
        }
    }
    
//    public boolean update(Building building) {
//        return true;
//    }
    
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
    
    public boolean isInTown(Building town, Vector v) {
        return (!town.hasClearance(v, 0));
    }
    
    public String canCreateBuilding(Player p, Location location, BuildingType bt) {
        UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(p);
        Vector v = location.toVector();
        v.setY(0);
        Building town = getTown(p.getUniqueId().toString());
        if (town != null) {
            if (bt == BuildingType.TOWN) {
                return "You already have a settlement.";
            } else if (!town.hasClearance(location.toVector(), -3)) { // within town
                for (Building b : buildings) { // Checks not overlapping a current building
                    if (b.type != BuildingType.TOWN && !b.hasClearance(v, 1)) {
                        up.clearBorders();
                        up.showBorder(b);
                        return "Too close to another building.";
                    }
                }
                return null;
            } else if (town.hasClearance(v, 0)) {
                return "Building needs to be inside a settlement.";
            } else {
                up.clearBorders();
                up.showBorder(town);
                return "Too close to settlement borders.";
            }
        } else {
            if (bt == BuildingType.TOWN) {
                return null;
            } else {
                return "You need to build a settlement first";
            }
        }
    }
    
    public String canDestroyBuilding(Player p, Building b) {
        if (b.getOwner().equals(p.getUniqueId())) {
            if (b.type != BuildingType.TOWN) {
                return null;
            } else {
                if (this.getTownBuildings(b).size() > 0) {
                    return "You must destroy all buildings/regions inside first";
                }
            }
        } else {
            return "You are not the owner of this region.";
        }
        return null;
    }
    
    public Building getTown(String uuid) {
        for (Building b : buildings) {
            if (b.type == BuildingType.TOWN && (b.hasMember(uuid) || b.getOwner().equals(uuid))) {
                return b;
            }
        }
        return null;
    }
    
    public List<Building> getTowns() {
        List<Building> towns = new ArrayList<>();
        for (Building b : buildings) {
            if (b.type == BuildingType.TOWN) {
                towns.add(b);
            }
        }
        return towns;
    }
    
    public List<Building> getTownShops(Building town) {
        List<Building> buildings = this.getTownBuildings(town);
        List<Building> shops = new ArrayList<>();
        for (Building b : buildings) {
            if (b.type == BuildingType.SHOP) {
                shops.add(b);
            }
        }
        return shops;
    }
    
    public boolean hasTown(Player p) {
        return (getTown(p.getUniqueId().toString())!=null);
    }
    
    
    
    public void removeMember(Building town, String uuid) {
        Iterator it = buildings.iterator();
        while (it.hasNext()) {
            Building b = (Building) it.next();
            if (b.getOwner().equals(uuid)) {
                buildings.remove(b);
            }
        }
        town.subowners.remove(uuid);
    }
}
