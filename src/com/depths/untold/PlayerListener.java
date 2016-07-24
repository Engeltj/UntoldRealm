package com.depths.untold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

/**
 *
 * @author Tim
 */
public class PlayerListener implements Listener {
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) { 
        if ((event.getMessage().contains("Tim") || event.getMessage().contains("tim")) && (event.getMessage().contains("best"))){
            event.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("Untold"), new Runnable() {
                public void run() {
                    Bukkit.broadcastMessage(ChatColor.GRAY+"Arnold Schwarzenegger:" +ChatColor.RESET+" Yes he is.");
                }
            }, 40);
        }     
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("build")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("capital")) {
                ItemStack is = new ItemStack(Material.STICK);
                ItemMeta meta = (ItemMeta) is.getItemMeta();
                is.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                List<String> lore = new ArrayList<String>();
                lore.add("A tool for constructing a capital");
                meta.setLore(lore);
                meta.setDisplayName("Capital Builder");
                is.setItemMeta(meta);
                
                Player p = (Player) sender;
                PlayerInventory inv = p.getInventory();
                inv.addItem(is);
            } else {
                sender.sendMessage(
                        "/build <options>\n" +
                        "========OPTIONS=======\n" +
                        "capital - Your main capital, required for other buildings"
                );
            }
            return true;
        }
        return false;
    }
}
