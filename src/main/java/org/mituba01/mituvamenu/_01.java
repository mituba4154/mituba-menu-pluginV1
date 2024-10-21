package org.mituba01.mituvamenu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class _01 extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        getLogger().info("Config loaded. Menu items: " + getConfig().getList("menu-items"));

        getCommand("menu").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                openMainMenu((Player) sender);
                return true;
            }
            return false;
        });
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Mituvamenu plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Mituvamenu plugin has been disabled!");
    }

    private void openMainMenu(Player player) {
        getLogger().info("Opening menu for player: " + player.getName());
        Inventory inventory = Bukkit.createInventory(null, 54, "Mituva Menu");

        // Fill with gray glass panes
        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = grayGlass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            grayGlass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, grayGlass);
        }

        // Add menu items
        List<ItemStack> menuItems = getMenuItemsFromConfig();
        getLogger().info("Loaded " + menuItems.size() + " menu items from config");
        for (ItemStack item : menuItems) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasLore()) {
                List<String> lore = meta.getLore();
                if (lore != null && !lore.isEmpty()) {
                    String locationStr = lore.get(0);
                    getLogger().info("Processing item: " + meta.getDisplayName() + " at location: " + locationStr);
                    String[] locationParts = locationStr.split(",");
                    try {
                        int row = Integer.parseInt(locationParts[0].trim());
                        int col = Integer.parseInt(locationParts[1].trim());
                        int slot = (row - 1) * 9 + (col - 1);
                        if (slot >= 0 && slot < 54) {
                            inventory.setItem(slot, item);
                            getLogger().info("Placed item " + meta.getDisplayName() + " at slot " + slot);
                        } else {
                            getLogger().warning("Invalid slot calculated: " + slot + " for item " + meta.getDisplayName());
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        getLogger().warning("Error parsing location for item " + meta.getDisplayName() + ": " + locationStr);
                    }
                }
            }
        }

        player.openInventory(inventory);
        getLogger().info("Opened menu for player: " + player.getName());
    }

    private List<ItemStack> getMenuItemsFromConfig() {
        List<ItemStack> items = new ArrayList<>();
        List<?> menuItemsConfig = getConfig().getList("menu-items");
        if (menuItemsConfig != null) {
            for (Object obj : menuItemsConfig) {
                if (obj instanceof Map) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = (Map<String, Object>) obj;
                        String location = (String) itemMap.get("location");
                        String materialName = (String) itemMap.get("material");
                        String name = (String) itemMap.get("name");
                        String command = (String) itemMap.get("command");

                        getLogger().info("Creating item: " + name + " (" + materialName + ") at " + location);

                        Material material = Material.valueOf(materialName.toUpperCase());
                        ItemStack item = new ItemStack(material);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(ChatColor.YELLOW + name);
                            List<String> lore = new ArrayList<>();
                            lore.add(location);
                            lore.add(command);
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                            getLogger().info("Item created successfully: " + name);
                        }
                        items.add(item);
                    } catch (Exception e) {
                        getLogger().warning("Error creating menu item: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        getLogger().info("Total items created: " + items.size());
        return items;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Mituva Menu")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            getLogger().info("Clicked item: " + clickedItem.getType() + " at slot " + event.getSlot());
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null) {
                getLogger().info("Item name: " + meta.getDisplayName());
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore != null && lore.size() >= 2) {
                        String command = lore.get(1);
                        getLogger().info("Executing command: " + command);
                        player.performCommand(command.substring(1)); // Remove the leading '/'
                    }
                }
            }

            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals("Mituva Menu")) {
            event.setCancelled(true);
        }
    }
}