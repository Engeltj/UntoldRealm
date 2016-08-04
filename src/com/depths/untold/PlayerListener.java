package com.depths.untold;

import com.depths.untold.Buildings.Building;
import com.depths.untold.Buildings.BuildingType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.bukkit.inventory.EquipmentSlot;
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
                p.sendMessage(ChatColor.RED+"Can't build here, too close to another building.");
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        if (!plugin.getBuildingManager().canBuild(p, loc)){
            p.sendMessage(ChatColor.RED+"Can't build here, too close to another building.");
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
        if (event.getHand() == EquipmentSlot.HAND && event.hasItem()) {
            ItemMeta im = event.getItem().getItemMeta();
            if (im.getDisplayName().contains("Build Tool")) {
                Player p = event.getPlayer();
                UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(p);
                if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {  // Tool selection
                    Inventory i = Bukkit.createInventory(p, 18, "Build Tools");
                    for (BuildingType bt : BuildingType.values()) {
                        ItemStack iss = new ItemStack(Material.SIGN);
                        ItemMeta imm = iss.getItemMeta();
                        imm.setDisplayName(bt.name());
                        if (bt == BuildingType.REGION) {
                            iss.setType(Material.STICK);
                            List<String> lore = new ArrayList<String>();
                            lore.add("Tool for creating protection regions");
                            imm.setLore(lore);
                        }
                        iss.setItemMeta(imm);
                        i.addItem(iss);
                    }

                    p.openInventory(i);
                    event.setCancelled(true);
                } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) { // Create region
                    Location loc = event.getClickedBlock().getLocation();
                    Building b = plugin.getBuildingManager().getBuilding(loc);
                    if (up.isModifyingBuilding()) {
                        up.modifyBuilding(p, b, loc);
                        p.sendMessage(ChatColor.GREEN + "Region resized.");
                    } else if (b != null && b.owner.equals(p.getUniqueId())) { // if is a building and owned by player
                        up.modifyBuilding(p, b, loc);
                        p.sendMessage(ChatColor.GREEN + "Select where you wish to expand region to");
                    } else {
                        for (BuildingType bt : BuildingType.values()) {
                            if (im.getDisplayName().contains(bt.name())) {
                                String can = plugin.getBuildingManager().canCreateBuilding(p, loc, bt);
                                if (can == null) {
                                    if (!plugin.getPlayerManager().getUntoldPlayer(p).hitQuotaLimit(bt)) {
                                        Building _b = plugin.getBuildingManager().create(p, loc, bt);
                                        _b.showBorder(p);
                                        p.sendMessage(ChatColor.GREEN + bt.name() + " built.");
                                    } else {
                                        if (bt == BuildingType.CAPITAL) {
                                            p.sendMessage(ChatColor.RED+"You already have a capital.");
                                        } else {
                                            p.sendMessage(ChatColor.RED+"You've hit the limit for this building type.");
                                        }
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED+can);
                                }
                                break;
                            }
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
