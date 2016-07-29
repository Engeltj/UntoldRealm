package com.depths.untold;


import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
/**
 *
 * @author Tim
 */
public final class Untold extends JavaPlugin {
    private PlayerListener playerListener;
    private Buildings buildings;
//    private HashMap<String, UntoldPlayer> players;
    private Economy economy;
    private static final String PLUGIN_NAME = ChatColor.GREEN + "Untold" + ChatColor.RESET;
    private double interestRate = 0;
    
    public Untold() {
        
    }
    
    @Override
    public void onEnable(){
//        players = new HashMap<String, UntoldPlayer>();
        playerListener = new PlayerListener();
        
        PluginManager pm = getServer().getPluginManager();
        if (!setupEconomy() ) {
            getLogger().info(String.format("[%s] - Disabled due to no instance of Vault found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        MySQL.connect();
        getLogger().info(MySQL.isConnected()? "connected": "no connection!!!!!!!");
        
        
        buildings = new Buildings();
        buildings.load();
        getLogger().info(PLUGIN_NAME + ": Buildings loaded.");
        
        pm.registerEvents(new PlayerListener(), this);
//        getServer().getScheduler().scheduleSyncRepeatingTask(this, new TimeSave(this), 10*60*20, 10*60*20);

        getLogger().info(PLUGIN_NAME + " by Depths has been enabled");
    }
 
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(playerListener);
        this.getServer().getScheduler().cancelTasks(this);
        getLogger().info(PLUGIN_NAME + " by Engeltj has been disabled");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return playerListener.onCommand(sender, cmd, label, args);
    }

    public void sendConsole(String message){
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
    
    public Buildings getBuildingsManager() {
        return this.buildings;
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
         } catch (Exception ignored) {}
         return null;
     }
}
