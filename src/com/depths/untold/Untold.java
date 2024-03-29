package com.depths.untold;

import com.depths.untold.Buildings.BuildingType;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.LogManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Tim
 */
public final class Untold extends JavaPlugin {

//    static {
//        LogManager.getLogManager().reset();
//    }

    private PlayerListener playerListener;
    private Buildings buildings;

    private Economy economy;
    private static final String PLUGIN_NAME = ChatColor.GREEN + "Untold" + ChatColor.RESET;

    private UntoldPlayers untoldPlayers;
    private final Untold plugin = this;


    @Override
    public void onEnable() {
        playerListener = new PlayerListener();
        untoldPlayers = new UntoldPlayers();

        PluginManager pm = getServer().getPluginManager();
        if (!setupEconomy()) {
            getLogger().info(String.format("[%s] - Disabled due to no instance of Vault found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        MySQL.connect();
        getLogger().info(MySQL.isConnected() ? "connected" : "no connection!!!!!!!");

        buildings = new Buildings();
        buildings.load();
        getLogger().info(PLUGIN_NAME + ": Buildings loaded.");
        
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                

                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    Building b = plugin.getBuildingManager().getClosest(p);
                    UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(p);

                    if (up.lastRegion != b) { // region change
                        if (up.lastRegion != null && up.lastRegion.type != BuildingType.TOWN && b.type == BuildingType.TOWN) {
                            up.lastRegion = b;
                        } else if (b.isInRegion(p)) {
                            up.lastRegion = b;
                            if (b.welcome_msg.length() > 0) {
                                p.sendMessage(b.welcome_msg);
                            }
                        } else {
                            up.lastRegion = null;
                        }
                    } else if (up.lastRegion != null) { // was in region, check if still in
                        if (!up.lastRegion.isInRegion(p)) { // if no longer is the region
                            up.lastRegion = null;
                        }
                    }
                }
            }
        }, 60L, 20L);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                getPlayerManager().updateWalkSpeeds();
            }
        }, 40L, 40L);

        pm.registerEvents(new PlayerListener(), this);
//        getServer().getScheduler().scheduleSyncRepeatingTask(this, new TimeSave(this), 10*60*20, 10*60*20);

        getLogger().info(PLUGIN_NAME + " by Depths has been enabled");
        
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(playerListener);
        this.getServer().getScheduler().cancelTasks(this);
        getLogger().info(PLUGIN_NAME + " by Depths has been disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return playerListener.onCommand(sender, cmd, label, args);
    }

    public void sendConsole(String message) {
        getLogger().info(message);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public Buildings getBuildingManager() {
        return this.buildings;
    }

    public UntoldPlayers getPlayerManager() {
        return untoldPlayers;
    }

    public String intToString(int num, int digits) {
        assert digits > 0 : "Invalid number of digits";
        // create variable length array of zeros
        char[] zeros = new char[digits];
        Arrays.fill(zeros, '0');
        // format number as String
        DecimalFormat df = new DecimalFormat(String.valueOf(zeros));
        return df.format(num);
    }

    public String md5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (Exception ignored) {
        }
        return null;
    }
}
