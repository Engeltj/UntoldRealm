/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.depths.untold;


import java.util.List;
import java.util.Map;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Tim
 */
public class Commands implements Listener{
    private Untold plugin;
    private CommandSender sender;
    private Command cmd;
    private String label;
    private String[] args;
    
    public Commands(){
        
    }
    
    public Commands(Untold plugin, CommandSender sender, Command cmd, String label, String[] args) {
        this.sender = sender;
        this.plugin = plugin;
        this.cmd = cmd;
        this.label = label;
        this.args = args;
    }
    
    
    public boolean executeCommand(){
        String command = cmd.getName();
//        if (command.equalsIgnoreCase("life"))
//            if (args.length == 0){
//            sender.sendMessage(ChatColor.YELLOW + "Time format: YYYY/WW/DD/HH/MM/SS");
//        }
        return true;
    }
    
}
