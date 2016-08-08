package com.depths.untold;

import com.depths.untold.Building;
import com.depths.untold.Buildings.BuildingType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
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
        if ((event.getMessage().contains("Tim") || event.getMessage().contains("tim")) && (event.getMessage().contains("best"))) {
            event.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("Untold"), new Runnable() {
                public void run() {
                    Bukkit.broadcastMessage(ChatColor.GRAY + "Arnold Schwarzenegger:" + ChatColor.RESET + " Yes he is.");
                }
            }, 40);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop() != null) {
            ItemStack is = event.getItemDrop().getItemStack();
            if (is.hasItemMeta() && is.getItemMeta().getDisplayName().contains("Build Tool")) {
                event.getItemDrop().remove();
            }
        }

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.canBuild()) {
            Player p = event.getPlayer();
            Location loc = event.getBlock().getLocation();
            if (!plugin.getBuildingManager().canBuild(p, loc)) {
                p.sendMessage(ChatColor.RED + "Can't build here, area is protected by another player.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        if (!plugin.getBuildingManager().canBuild(p, loc)) {
            p.sendMessage(ChatColor.RED + "Can't build here, area is protected by another player.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        UntoldPlayer up = plugin.getPlayerManager().addUntoldPlayer(p);
        int bonus  = up.getDailyBonus();
        if (bonus > 0) {
            plugin.getEconomy().depositPlayer(p, bonus);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    p.sendMessage(ChatColor.GREEN + "You have just received a daily login bonus of " + bonus + " clownfish!");
                }
            }, 200L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerManager().removeUntoldPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        List<HumanEntity> viewers = inv.getViewers();
        if (viewers.size() > 0) {
            Player p = (Player) viewers.get(0);
            if (inv.getTitle().equals("Build Tools")) {
                ItemStack is = event.getCurrentItem();
                if (is != null && is.hasItemMeta()) {
                    ItemMeta im = is.getItemMeta();
                    final String itemName = im.getDisplayName();
                    for (ItemStack iss : p.getInventory().getContents()) {
                        if (iss == null) {
                            continue;
                        }
                        if (iss.hasItemMeta()) {
                            ItemMeta imm = iss.getItemMeta();
                            imm.setDisplayName("Build Tool - " + itemName);
                            iss.setType(Material.SIGN);
                            iss.setItemMeta(imm);
                            p.sendMessage(ChatColor.GREEN + "Build tool set to " + itemName);
                            p.closeInventory();
                            p.updateInventory();
                            event.setCancelled(true);
                            break;
                        }
                    }
                }
            } else {
                Location loc = inv.getLocation();
                if (loc != null) {
                    Building b = plugin.getBuildingManager().getBuilding(loc);
                    if (b != null) {
                        if (b.type == BuildingType.HOUSE) {
                            if (!b.getOwner().equals(p.getUniqueId())) {
                                p.sendMessage(ChatColor.RED + "This chest is protected by " + Bukkit.getServer().getPlayer(b.getOwner()).getName());
                                event.setCancelled(true);
                            }
                        } else {
                            Building town = plugin.getBuildingManager().getTown(p);
                            if (town == null || !plugin.getBuildingManager().isInTown(town, loc.toVector())) {
                                p.sendMessage(ChatColor.RED + "This chest is protected.");
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

//    @EventHandler
//    public void onPlayerChangeHandItem(PlayerItemHeldEvent event) {
//        UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(event.getPlayer());
//        plugin.getBuildingManager().
//    }
//    @EventHandler
//    public void onSignBreak(BlockBreakEvent e) {
//        if (e.getBlock().getType() == Material.SIGN || e.getBlock().getType() == Material.SIGN_POST) {
//            Player p = e.getPlayer();
//            UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(p);
//            Sign sign = (Sign) e.getBlock().getState();
//            String line1 = sign.getLine(1);
//            for (BuildingType bt : BuildingType.values()) {
//                if (line1.contains(bt.name())) {
//                    Building b = plugin.getBuildingManager().getBuilding(sign.getLocation());
//                    if (b != null) {
//                        if (b.hasMember(p)) {
//                            if (b.type == BuildingType.TOWN && plugin.getBuildingManager().getTownBuildings(b).size() > 0) {
//                                p.sendMessage(ChatColor.GREEN + "Cannot disband settlement until all buildings within are disbanded.");
//                                e.setCancelled(true);
//                            } else {
//                                up.clearBorders();
//                                plugin.getBuildingManager().destroy(b);
//                                p.sendMessage(ChatColor.GREEN + StringUtils.capitalize(bt.name()) + " destroyed.");
//                            }
//                        } else {
//                            e.setCancelled(true);
//                        }
//                    }
//                    break;
//                }
//            }
//        }
//    }
    @EventHandler
    public void onPlayerExp(PlayerExpChangeEvent event) {
        Player p = event.getPlayer();
        int amount = event.getAmount();
        if (amount > 0) {
            UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(p);
            up.addExperience(amount);
        }
    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(p);
            int amount = event.getDroppedExp();
            up.addExperience(amount * -1); // avoids exp death farming
        }
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {

        if (event.getWorld().getName().equals("world")) {
            plugin.sendConsole("Saving Untold");
            plugin.getPlayerManager().savePlayers();
            plugin.getBuildingManager().save();
        }
    }

    @EventHandler
    public void onPlayerChangeHand(PlayerItemHeldEvent event) {
        Player p = event.getPlayer();
        UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(p);
        up.clearBorders();
    }

    @EventHandler
    public void onPlayerSetRegionName(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && event.hasItem()) {
            ItemStack is = event.getItem();
            if (is.getType() == Material.WRITTEN_BOOK) {
                BookMeta bm = (BookMeta) is.getItemMeta();
                if (bm.getTitle().equalsIgnoreCase("region")) {
                    Player p = event.getPlayer();
                    Block block = event.getClickedBlock();
                    if (block != null) {
                        Building b = plugin.getBuildingManager().getBuilding(block.getLocation());
                        if (b != null) {
                            if (b.getOwner().equals(p.getUniqueId())) {
                                String[] lines = bm.getPage(1).split("\n");
                                if (lines.length == 0) {
                                    b.name = "";
                                    b.welcome_msg = "";
                                    p.sendMessage(ChatColor.GREEN + "Region name and welcome message removed.");
                                }
                                if (lines.length > 0) {
                                    b.name = ChatColor.stripColor(lines[0]);
                                    b.name = ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', b.name);
                                    p.sendMessage(ChatColor.GREEN + "Region name set to: " + b.name);
                                }
                                if (lines.length > 1) {
                                    b.welcome_msg = ChatColor.stripColor(lines[1]);
                                    b.welcome_msg = ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', b.welcome_msg);
                                    plugin.sendConsole(b.welcome_msg);
                                    p.sendMessage(ChatColor.GREEN + "Region welcome message set to: " + b.welcome_msg);
                                } else if (b.welcome_msg.length() > 0) {
                                    b.welcome_msg = "";
                                    p.sendMessage(ChatColor.GREEN + "Region welcome message removed");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Only the owner can modify this region");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Use book on a block in a valid region");
                        }
                        event.setCancelled(true);
                    }

                }
            }
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && event.hasItem()) {
            ItemMeta im = event.getItem().getItemMeta();
            String dn = (im != null) ? im.getDisplayName() : null;
            if (dn != null) {
                if (dn.contains("Build Tool")) {
                    Player p = event.getPlayer();
                    UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(p);
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {  // Tool selection
                        Inventory i = Bukkit.createInventory(p, 18, "Build Tools");

                        //Add tools
                        for (BuildingType bt : BuildingType.values()) {
                            ItemStack iss;
                            ItemMeta imm;
                            if (bt == BuildingType.TOWN) {
                                iss = new ItemStack(Material.PURPUR_BLOCK);

                            } else {
                                iss = new ItemStack(Material.END_BRICKS);
                            }
                            imm = iss.getItemMeta();
                            imm.setDisplayName(StringUtils.capitalize(bt.name().toLowerCase()));
                            String desc = plugin.getBuildingManager().DESCRIPTIONS.get(bt);
                            ArrayList<String> lore = new ArrayList<String>();
                            int j = 0;
                            while (j < desc.length()) { // Word wrap descriptions
                                int new_j = desc.indexOf(" ", j + 25);
                                if (new_j < 0) {
                                    new_j = desc.length();
                                }
                                lore.add(desc.substring(j, new_j).trim());
                                j = new_j;
                            }
                            imm.setLore(lore);

                            iss.setItemMeta(imm);
                            i.addItem(iss);
                        }
                        //Add info item
                        ItemStack is = new ItemStack(Material.STRING);
                        ArrayList<String> lore = new ArrayList<String>();
                        lore.add("For showing the boundry of a region.");
                        lore.add("Right-click a block to show boundry");
                        ItemMeta imm = is.getItemMeta();
                        imm.setDisplayName("Region Viewer");
                        imm.setLore(lore);
                        is.setItemMeta(imm);
                        i.addItem(is);

                        p.openInventory(i);
                        event.setCancelled(true);
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) { // Create region
                        if (dn.contains("Region Viewer")) {
                            Block block = event.getClickedBlock();
                            if (block != null) {
                                Location loc = block.getLocation();
                                Building b = plugin.getBuildingManager().getBuilding(loc);
                                if (b != null) {
                                    up.clearBorders();
                                    up.showBorder(b);
                                    p.sendMessage(ChatColor.GREEN + "Region type: " + ChatColor.YELLOW + StringUtils.capitalize(b.type.name().toLowerCase()));
                                    p.sendMessage(ChatColor.GREEN + "Four corners of region are shown.");
                                } else {
                                    p.sendMessage(ChatColor.RED + "Not a region.");
                                }
                            }
                            event.setCancelled(true);
                            return;
                        }
                        Location loc = event.getClickedBlock().getLocation();
                        Building b = plugin.getBuildingManager().getBuildingFromCorner(loc);
                        if (up.isModifyingBuilding()) {
                            b = up.modifyBuilding(p, b, loc);
                            if (b != null) {
                                p.sendMessage(ChatColor.GREEN + StringUtils.capitalize(b.type.name().toLowerCase()) + " region resized.");
                            }
                        } else if (b != null && b.hasMember(p)) { // if is a building and owned by player
                            up.modifyBuilding(p, b, loc);
                            p.sendMessage(ChatColor.YELLOW + "Right-click where you wish to resize the region to");
                        } else {
                            for (BuildingType bt : BuildingType.values()) {
                                if (StringUtils.containsIgnoreCase(im.getDisplayName(), bt.name())) {
                                    String can = plugin.getBuildingManager().canCreateBuilding(p, loc, bt);
                                    if (can == null) {
                                        if (!plugin.getPlayerManager().getUntoldPlayer(p).hitQuotaLimit(bt)) {
                                            Building _b = plugin.getBuildingManager().create(p, loc, bt);
//                                            Block block = loc.getBlock();
//                                            Block above = block.getRelative( BlockFace.UP );
//                                            above.setType( Material.SIGN_POST );
//                                            Sign sign = (Sign) above.getState();
//                                            sign.setLine(1, bt.name());
//                                            sign.update();
                                            up.clearBorders();
                                            up.showBorder(_b);
                                            p.sendMessage(ChatColor.GREEN + bt.name() + " built.");
                                            p.sendMessage(ChatColor.YELLOW + "To expand region, right-click a corner");
                                        } else if (bt == BuildingType.CAPITAL) {
                                            p.sendMessage(ChatColor.RED + "You already have a capital.");
                                        } else {
                                            p.sendMessage(ChatColor.RED + "You've hit the limit for this building type.");
                                        }
                                    } else {
//                                        Building _b = plugin.getBuildingManager().getBuilding(loc);
//                                        if (_b != null) {
//                                            up.clearBorders();
//                                            up.showBorder(_b);
//                                        }
                                        p.sendMessage(ChatColor.RED + can);
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
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("u") || cmd.getName().equalsIgnoreCase("untold")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("tool")) {
                    ItemStack is = new ItemStack(Material.STICK);
                    ItemMeta meta = (ItemMeta) is.getItemMeta();
                    List<String> lore = new ArrayList<String>();
                    lore.add("A tool for constructing buildings & regions");
                    meta.setLore(lore);
                    meta.setDisplayName("Build Tool");
                    is.setItemMeta(meta);
                    is.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                    PlayerInventory inv = p.getInventory();
                    inv.addItem(is);
                    return true;
                } else if (args[0].equalsIgnoreCase("destroy")) {
                    final UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(p);
                    Building b = plugin.getBuildingManager().getBuilding(p.getLocation());
                    if (b != null) {
                        String msg = plugin.getBuildingManager().canDestroyBuilding(p, b);
                        if (msg == null) { // allowed
                            if (up.destroyRegion == b) {
                                plugin.getBuildingManager().destroy(b);
                                p.sendMessage(ChatColor.GREEN + "Region destroyed.");
                            } else {
                                up.destroyRegion = b;
                                Bukkit.getScheduler().cancelTask(up.destroyRegionExpire);
                                up.destroyRegionExpire = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    public void run() {
                                        up.destroyRegion = null;
                                    }
                                }, 600L);
                            }
                            p.sendMessage(ChatColor.YELLOW + "Type '/b destroy' again to confirm.");
                        } else {
                            p.sendMessage(ChatColor.RED + msg);
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Please stand inside a region first.");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("destroy")) {
                    UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(p);
                    p.sendMessage(ChatColor.GREEN + "Current Tier: " + ChatColor.YELLOW + up.getTier());
                    p.sendMessage(ChatColor.GREEN + "Total Level: " + ChatColor.YELLOW + up.getLevel());
                    p.sendMessage(ChatColor.GREEN + "Experience Gained: " + ChatColor.YELLOW + up.getExperience());
                    return true;
                } else if (args[0].equalsIgnoreCase("taxes")) {
                    UntoldPlayer up = plugin.getPlayerManager().getUntoldPlayer(p);
                    p.sendMessage(ChatColor.GREEN + "Current Tier: " + ChatColor.YELLOW + up.getTier());
                    p.sendMessage(ChatColor.GREEN + "Total Level: " + ChatColor.YELLOW + up.getLevel());
                    p.sendMessage(ChatColor.GREEN + "Experience Gained: " + ChatColor.YELLOW + up.getExperience());
                    return true;
                }
                
            }
            
            p.sendMessage(ChatColor.GREEN + "Usage: '/u <option>'. Your available options are:");
            p.sendMessage(ChatColor.YELLOW + "tool" + ChatColor.GREEN + " - Spawn a build tool in your inventory");
            p.sendMessage(ChatColor.YELLOW + "destroy" + ChatColor.GREEN + " - Destroy a region you are standing in");
            p.sendMessage(ChatColor.YELLOW + "exp" + ChatColor.GREEN + " - Shows your current tier, total exp, and level");
            p.sendMessage(ChatColor.YELLOW + "taxes" + ChatColor.GREEN + " - Shows your daily land tax");
            return true;
        } else if (cmd.getName()
                .equalsIgnoreCase("untold")) {
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
