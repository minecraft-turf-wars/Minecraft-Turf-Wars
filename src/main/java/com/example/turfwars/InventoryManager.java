package com.example.turfwars;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// Helper class for inventory managment
public class InventoryManager {
    
    // The starting combat kit for players
    public static void giveCombatKit(Player player, Material teamBlock){
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().addItem(new ItemStack(Material.BOW, 1));
        player.getInventory().addItem(new ItemStack(Material.ARROW, 7));
        player.getInventory().addItem(new ItemStack(teamBlock, 7));
    }

    // The continuously replenishing items 
    public static void giveReplenishItems(Player player, Material teamBlock){
        player.getInventory().addItem(new ItemStack(teamBlock, 1));
        player.getInventory().addItem(new ItemStack(Material.ARROW, 1));
    }

    // Reset stats
    public static void resetHealtAndHunger(Player player){
        player.setHealth(20.0);
        player.setFoodLevel(20);
    }

    // Clear inventory
    public static void clearInventory(Player player){
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }
}
