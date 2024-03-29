package com.depths.untold;


import com.depths.untold.UntoldPlayer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import static org.depths.untold.generated.Tables.PLAYERS;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author tim
 */
public class UntoldPlayers {
    private Map<UUID, UntoldPlayer> players = new HashMap<UUID, UntoldPlayer>();
    private Untold plugin;
    
    public UntoldPlayers() {
        plugin = (Untold) Bukkit.getServer().getPluginManager().getPlugin("Untold");
    }
    
    public void crunchShopEarnings() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        Timestamp ts = new Timestamp(cal.getTime().getTime());
        DSLContext db = DSL.using(MySQL.getConnection(), SQLDialect.MYSQL);
        Record[] records = db.select().from(PLAYERS).where(PLAYERS.LASTLOGIN.greaterOrEqual(ts)).fetchArray();
        
        List<Building> towns = plugin.getBuildingManager().getTowns();
        for (Building town : towns) {
            List<Building> shops = plugin.getBuildingManager().getTownShops(town);
            int shop_count = shops.size();
            List<String> members = town.getAllMembers();
            int shop_earnings = shop_count * Buildings.SHOP_INCOME / members.size();
            int bonus_earnings = (int)((members.size()-1) * 0.05 * shop_earnings);
            for (String uuid : members) {
                OfflinePlayer op = plugin.getServer().getOfflinePlayer(UUID.fromString(uuid));
                plugin.getEconomy().depositPlayer(op, shop_earnings + bonus_earnings);
                if (op.isOnline()) {
                    Player p = (Player) op;
                    p.sendMessage(ChatColor.GREEN+"You've received " + (shop_earnings + bonus_earnings) + ChatColor.GOLD + " cf " + ChatColor.GREEN+ "as your share of income.");
                    p.sendMessage(ChatColor.GOLD +""+ bonus_earnings + " cf of this was bonus (+5% for every member of the town)");
                }
            }
        }
        
        
        for (Record r : records) {
            
        }
    }
    
    public void updateWalkSpeeds() {
        float max_items = 2304;
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            Inventory i = p.getInventory();
            ItemStack[] iss = i.getContents();
            float total = 0f;
            for (ItemStack is : iss) {
                if (is != null)
                    total += is.getAmount();
            }
            float delta = 1f - (max_items - total) / max_items;
            float speed = (2.5f - delta)/10f;
            speed = (float) Math.ceil(speed * 100)/100f;
            if (p.getWalkSpeed() != speed)
                p.setWalkSpeed(speed);
        }
    }
    
    public UntoldPlayer getUntoldPlayer(Player p) {
        if (!players.containsKey(p.getUniqueId())) {
            return addUntoldPlayer(p);
        } else {
            return players.get(p.getUniqueId());
        }
    }
    
    public UntoldPlayer addUntoldPlayer(Player p) {
        if (!players.containsKey(p.getUniqueId())) {
            UntoldPlayer up = new UntoldPlayer(p);       
            players.put(p.getUniqueId(), up);
            return up;
        } else {
            return players.get(p.getUniqueId());
        }
    }
    
    public void removeUntoldPlayer(Player p) {
        UUID uuid = p.getUniqueId();
        if (players.containsKey(uuid)) {
            UntoldPlayer up = players.get(p.getUniqueId());
            up.save();
            players.remove(uuid);
        }
    }
    
    public void savePlayers() {
        Iterator it = players.entrySet().iterator();
        while (it.hasNext()) {
            Entry pair = (Entry) it.next();
            UntoldPlayer up = (UntoldPlayer) pair.getValue();
            up.save();
        }
    }
    
    
}
