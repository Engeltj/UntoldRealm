package com.depths.untold;

import com.depths.untold.Buildings.BuildingType;
import java.util.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import static org.depths.untold.generated.Tables.PLAYERS;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.milkbowl.vault.economy.EconomyResponse;
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

    private final Player p;
    private final UUID uuid;
    private final Untold plugin;
    private long experience = 0L;
    private Timestamp lastlogin;
    private Date lastBonus;

    transient private Vector move_building;
    transient public Building lastRegion = null; // for purposes of welcome/farewell messages from regions

    transient public Building destroyRegion = null; // for purposes of destroying a region (confirm dialog)
    transient public int destroyRegionExpire = -1; // for purposes of destroying a region (expiration of request)

    private final Map<BuildingType, Integer> quotas = new HashMap<BuildingType, Integer>();
    transient private ArrayList<Location> borders = new ArrayList<Location>(); // cache for visible building boarders

    public UntoldPlayer(Player p) {
        this.p = p;
        this.plugin = (Untold) Bukkit.getServer().getPluginManager().getPlugin("Untold");
        this.uuid = p.getUniqueId();
        for (BuildingType b : BuildingType.values()) {
            quotas.put(b, 1);
        }
        quotas.put(BuildingType.HOUSE, 100);
        load();
        
        quotas.put(BuildingType.SHOP, getTier());
    }

    public void addExperience(int exp) {
        experience += exp;
    }

    public long getExperience() {
        return experience;
    }

    public int getLevel() {
        if (experience <= 255) {
            return (int) experience / 17;
        } else if (experience > 272 && experience < 887) {
            return (int) ((Math.sqrt(24 * experience - 5159) + 59) / 6);
        } else if (experience > 825) {
            return (int) ((Math.sqrt(56 * experience - 32511) + 303) / 14);
        }
        return 0;
    }
    
    public int getTier() {
        int level = getLevel();
        if (level < 20) {
            return 1;
        } else if (level < 40) {
            return 2;
        } else if (level < 60) {
            return 3;
        } else if (level < 80) {
            return 4;
        } else if (level < 100) {
            return 5;
        } else if (level < 120) {
            return 6;
        } else {
            return 7;
        }
    }

    public int getDailyBonus() {
        if (lastBonus == null) {
            return 100;
        }
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(lastBonus);
        cal2.setTime(new Date());
        if (cal1.get(Calendar.DAY_OF_YEAR) != cal2.get(Calendar.DAY_OF_YEAR)) {
            lastBonus = cal2.getTime();
            return 100;
        } else {
            return 0;
        }
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

            int cost = _b.getResizeCost(corners);
            if (cost > 0) {
                EconomyResponse r = plugin.getEconomy().withdrawPlayer(_p, _p.getWorld().getName(), cost);
                if (!r.transactionSuccess()) {
                    _p.sendMessage(ChatColor.RED + "Insufficient funds, this operation will cost " + ChatColor.GOLD + cost + " clownfish" + ChatColor.RED + ".");
                    return null;
                }
            } else if (cost < 0) {
                plugin.getEconomy().depositPlayer(_p, _p.getWorld().getName(), cost * -1);
                _p.sendMessage(ChatColor.GREEN + "You were refunded " + cost * -1 + ChatColor.GOLD + " clownfish" + ChatColor.GREEN + ".");
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
            for (Vector v : b.corners) {
                Location loc = v.toLocation(p.getWorld());
                loc.setY(p.getLocation().getY() + 5);
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
        lastBonus = r.get(PLAYERS.DAILY_BONUS);
    }

    public void save() {
        DSLContext db = DSL.using(MySQL.getConnection(), SQLDialect.MYSQL);
        db.update(PLAYERS)
                .set(PLAYERS.NAME, p.getName())
                .set(PLAYERS.EXPERIENCE, experience)
                .set(PLAYERS.LASTLOGIN, lastlogin)
                .set(PLAYERS.DAILY_BONUS, new java.sql.Date(lastBonus.getTime()))
                .where(PLAYERS.UUID.equal(uuid.toString())).execute();
    }
}
