package com.depths.untold;

import com.depths.untold.Buildings.BuildingType;
import java.sql.Timestamp;
import java.util.ArrayList;
import static org.depths.untold.generated.Tables.PLAYERS;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
    private long experience = 0L;
    private Timestamp lastlogin;

    transient private Vector move_building;

    private final Map<BuildingType, Integer> quotas = new HashMap<BuildingType, Integer>();
    transient private ArrayList<Location> borders = new ArrayList<Location>(); // cache for visible building boarders

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

    public long getExperience() {
        return experience;
    }

    private int getBuildingCount(BuildingType bt) {
        int count = 0;
        for (Building b : plugin.getBuildingManager().getBuildings()) {
            if (bt == b.type && b.hasMember(uuid)) {
                count++;
            }
        }
        return count;
    }

    public boolean hitQuotaLimit(BuildingType b) {
        return quotas.get(b) - getBuildingCount(b) <= 0;
    }

    private static List<Vector> cloneVectorList(List<Vector> list) {
        List<Vector> clone = new ArrayList<Vector>(list.size());
        for (Vector item : list) {
            clone.add(item.clone());
        }
        return clone;
    }

    public Building modifyBuilding(Player p, Building b, Location loc) {
        Vector v = loc.toVector();
        v.setY(0);

        if (move_building == null) {
            move_building = v;
        } else {
            final Player _p = p;
            final UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(_p);
            final Building _b = plugin.getBuildingManager().getBuildingFromCorner(new Location(loc.getWorld(), move_building.getX(), move_building.getY(), move_building.getZ()));
            int idx = _b.corners.indexOf(move_building);
            List<Vector> corners = cloneVectorList(_b.corners);
            if (idx == 0) {
                corners.get(0).setX(loc.getX());
                corners.get(0).setZ(loc.getZ());
                corners.get(1).setZ(loc.getZ());
                corners.get(3).setX(loc.getX());
            } else if (idx == 1) {
                corners.get(1).setX(loc.getX());
                corners.get(1).setZ(loc.getZ());
                corners.get(0).setZ(loc.getZ());
                corners.get(2).setX(loc.getX());
            } else if (idx == 2) {
                corners.get(2).setX(loc.getX());
                corners.get(2).setZ(loc.getZ());
                corners.get(1).setX(loc.getX());
                corners.get(3).setZ(loc.getZ());
            } else if (idx == 3) {
                corners.get(3).setX(loc.getX());
                corners.get(3).setZ(loc.getZ());
                corners.get(0).setX(loc.getX());
                corners.get(2).setZ(loc.getZ());
            }
            
            if (_b.type != BuildingType.TOWN) {
                for (Vector corner : corners) {
                    Building town = plugin.getBuildingManager().getTown(p);
                    if (!plugin.getBuildingManager().isInTown(town, corner)) {
                        up.clearBorders();
                        up.showBorder(town);
                        p.sendMessage(ChatColor.RED + "Resize not possible, the building must stay within the town");
                        move_building = null;
                        return null;
                    }
                }
            } else {
                Building town = plugin.getBuildingManager().getTown(p);
                List<Building> buildings = plugin.getBuildingManager().getTownBuildings(town);
                for (Building b_town : buildings) {
                    for (Vector corner : b_town.corners) {
                        if (!plugin.getBuildingManager().isInTown(town, corner)) {
                            up.clearBorders();
                            up.showBorder(b_town);
                            p.sendMessage(ChatColor.RED + "Resize not possible, the highlighted building will become outside of the town.");
                            move_building = null;
                            return null;
                        }
                    }
                }
            }
            _b.corners = corners;
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    up.clearBorders();
                    up.showBorder(_b);
                }
            }, 1);
            move_building = null;
            return _b;
        }
        return null;
    }

    public boolean isModifyingBuilding() {
        return move_building != null;
    }
    
    public void clearBorders() {
        Player p = plugin.getServer().getPlayer(uuid);
        if (p != null && p.isOnline()) {
            for (Location loc : borders) {
                p.sendBlockChange(loc, loc.getBlock().getType(), (byte) 0);
            }
            borders.remove(uuid);
        }
        
    }
    
    public void showBorder(Building b) {
        Player p = plugin.getServer().getPlayer(uuid);
        if (p != null && p.isOnline()) {
            for (Vector v: b.corners) {
                Location loc = v.toLocation(p.getWorld());
                loc.setY(p.getLocation().getY()+5);
                while (loc.getBlock().isEmpty()) {
                    loc.subtract(0, 1, 0);
                }
                borders.add(loc);
                p.sendBlockChange(loc, Material.GLOWSTONE, (byte) 0);
            }
        }
    }

    private void load() {
        DSLContext db = DSL.using(MySQL.getConnection(), SQLDialect.MYSQL);
        Record r = db.select().from(PLAYERS).where(PLAYERS.UUID.equal(uuid.toString())).fetchOne();
        if (r == null) {
            plugin.sendConsole("New player found, creating for " + uuid.toString());
            db.insertInto(PLAYERS, PLAYERS.UUID).values(uuid.toString()).execute();
            r = db.select().from(PLAYERS).where(PLAYERS.UUID.equal(uuid.toString())).fetchOne();
        }
        lastlogin = r.get(PLAYERS.LASTLOGIN);
        experience = r.get(PLAYERS.EXPERIENCE);
    }

    public void save() {
        DSLContext db = DSL.using(MySQL.getConnection(), SQLDialect.MYSQL);
        db.update(PLAYERS)
                .set(PLAYERS.EXPERIENCE, experience)
                .set(PLAYERS.LASTLOGIN, lastlogin)
                .where(PLAYERS.UUID.equal(uuid.toString())).execute();
    }
}