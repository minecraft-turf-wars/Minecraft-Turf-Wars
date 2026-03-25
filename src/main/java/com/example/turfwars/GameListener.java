package com.example.turfwars;

import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;

public class GameListener implements Listener{
    private final GameManager GAME_MANAGER;

    public GameListener(GameManager GAME_MANAGER){
        this.GAME_MANAGER = GAME_MANAGER;
    }

    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent event){
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player killer)) return;

        Game game = findGame(victim);
        if(game == null || game.getState() != GameState.RUNNING) return;

        event.setDamage(1000.0);
        game.handleKill(killer);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        Game game = findGame(player);
        if (game == null) return;

        double blockPos = event.getBlock().getLocation().getX();

        if (game.getBlackTeam().contains(player.getUniqueId()) && blockPos > game.getDivideLine()){
            event.setCancelled(true);
            player.sendMessage("§cYou cannot build enemy territory!");
        }
    }

    @EventHandler
    public void onRespawn(PlayerPostRespawnEvent event){
        Player player = event.getPlayer();
        Game game = findGame(player);
        if (game == null || game.getState() != GameState.RUNNING){
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            return;
        }

        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);

        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.OAK_PLANKS, 7));
        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.ARROW, 7));
        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.BOW, 1));

        World world = game.getArenaWorld();
        Location respawnLoc;

        if (game.getBlackTeam().contains(player.getUniqueId())){
            respawnLoc = new Location (world, Game.BLACK_X, Game.BLACK_Y, Game.BLACK_Z);
        } else{
            respawnLoc = new Location (world, Game.GOLD_X, Game.GOLD_Y, Game.GOLD_Z);
        }
        Location spectatorLoc = new Location(world, -21, -59, 38);
        player.teleport(spectatorLoc);
        player.setGameMode(GameMode.SPECTATOR);
        Bukkit.getScheduler().runTaskLater(TurfWars.getInstance(), () -> {
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(respawnLoc);
            player.sendMessage("§aYou have respawned!");
        }, 60L); // 3-second respawn timer

    }


    private Game findGame(Player player){
        return GAME_MANAGER.getGames().stream()
            .filter(g -> g.getPlayers().contains(player.getUniqueId()))
            .findFirst().orElse(null);
    }
    

}
