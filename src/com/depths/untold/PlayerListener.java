package com.depths.untold;

import com.depths.untold.Buildings.Building;
import com.depths.untold.Buildings.BuildingType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;


/**
 *
 * @author Tim
 */
public class PlayerListener implements Listener {
    private static Untold plugin;
    
    public PlayerListener() {
        plugin = (Untold) Bukkit.getServer().getPluginManager().getPlugin("Untold");
    }
    
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
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop() != null){
            ItemStack is = event.getItemDrop().getItemStack();
            if (is.getItemMeta().getDisplayName().equals("Capital Builder")){
                event.getItemDrop().remove();
            }
        }
        
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.canBuild()) {
            Player p = event.getPlayer();
            Location loc = event.getBlock().getLocation();
            if (!plugin.getBuildingManager().canBuild(p, loc)){
                p.sendMessage(ChatColor.RED+"Can't build here, too close to another players building.");
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        if (!plugin.getBuildingManager().canBuild(p, loc)){
            p.sendMessage(ChatColor.RED+"Can't build here, too close to another players building.");
            event.setCancelled(true);
        }
    }
    
    
    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event){
        Inventory inv = event.getInventory();
        List<HumanEntity> viewers = inv.getViewers();
        if (viewers.size()>0 && inv.getTitle().equals("Build Tools")){
            Player p = (Player) viewers.get(0);
            ItemStack is = event.getCurrentItem();
            if (is != null && is.getItemMeta() != null){
                ItemMeta im = is.getItemMeta();
                String itemName = im.getDisplayName();
                for (ItemStack iss : p.getInventory().getContents()) {
                    if (iss == null) continue;
                    ItemMeta imm = iss.getItemMeta();
                    
                    if (iss.hasItemMeta() && imm.getDisplayName().contains("Build Tool")) {
                        imm.setDisplayName("Build Tool - " + itemName);
                        iss.setItemMeta(imm);
                        p.sendMessage(ChatColor.GREEN + "Build tool set to " + itemName);
                        p.closeInventory();
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action a = event.getAction();
        ItemStack is = event.getItem();
        if (is != null && (a == Action.RIGHT_CLICK_BLOCK || a == Action.LEFT_CLICK_BLOCK || a == Action.LEFT_CLICK_AIR)) {
            ItemMeta im = event.getItem().getItemMeta();
            if (im != null && im.getDisplayName() != null && im.getDisplayName().contains("Build Tool")) {
                final Player p = event.getPlayer();
                if (a == Action.LEFT_CLICK_BLOCK || a == Action.LEFT_CLICK_AIR) {
                    Inventory i = Bukkit.createInventory(p, 9, "Build Tools");
                    for (BuildingType bt : BuildingType.values()) {
                        ItemStack iss = new ItemStack(Material.STICK);
                        ItemMeta imm = is.getItemMeta();
                        imm.setDisplayName(bt.name());
                        iss.setItemMeta(imm);
                        i.addItem(iss);
                    }
                    
                    p.openInventory(i);
                    event.setCancelled(true);
                } else {
                    Location loc = event.getClickedBlock().getLocation();
                    for (BuildingType bt : BuildingType.values()) {
                        if (im.getDisplayName().contains(bt.name())) {
                            if (plugin.getBuildingManager().canCreateBuilding(p, loc, bt)) {
                                if (!plugin.getPlayerManager().getUntoldPlayer(p).hitQuotaLimit(bt)) {
                                    Building _b = plugin.getBuildingManager().create(p, loc, bt);
                                    _b.showBorder(p);
                                    p.sendMessage(ChatColor.GREEN + bt.name() + "built.");
                                } else {
                                    if (bt == BuildingType.CAPITAL) {
                                        p.sendMessage(ChatColor.RED+"You already have a capital.");
                                    } else {
                                        p.sendMessage(ChatColor.RED+"You've hit the limit for this building type.");
                                    }
                                }
                            } else {
                                p.sendMessage(ChatColor.RED+"Too close to another players building");
                            }
                            break;
                        }
                    }
                    event.setCancelled(true);
                }
            }
        }
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("build")) {
            ItemStack is = new ItemStack(Material.STICK);
            ItemMeta meta = (ItemMeta) is.getItemMeta();
            List<String> lore = new ArrayList<String>();
            lore.add("A tool for constructing a buildings");
            meta.setLore(lore);
            meta.setDisplayName("Build Tool");
            is.setItemMeta(meta);
            is.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

            Player p = (Player) sender;
            PlayerInventory inv = p.getInventory();
            inv.addItem(is);
            
            return true;
        } else if (cmd.getName().equalsIgnoreCase("untold")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                plugin.onDisable();
                plugin.onEnable();
                sender.sendMessage("Untold plugin reloaded");
                return true;
            }
        }
        return false;
    }
}
