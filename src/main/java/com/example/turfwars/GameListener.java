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

    // Handles one-shot logic for arrow hits
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

    // Restricts block placing on the enemy turf
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        Game game = findGame(player);
        if (game == null) return;

        double blockZ = event.getBlock().getLocation().getZ();
        double divideLine = game.getDivideLine();
        
        // Handles black team block placement
        if (game.getBlackTeam().contains(player.getUniqueId())){
            if (blockZ > divideLine){
                event.setCancelled(true);
                player.sendMessage("§cYou cannot build enemy territory!");
            }
        }
        // Handles gold team block placement
        else if (game.getGoldTeam().contains(player.getUniqueId())){
            if (blockZ < divideLine){
                event.setCancelled(true);
                player.sendMessage("§cYou cannot build enemy territory!");
            }
        }
    }

    // Handles respawn during gamemode
    @EventHandler
    public void onRespawn(PlayerPostRespawnEvent event){
        Player player = event.getPlayer();
        Game game = findGame(player);
        if (game == null || game.getState() != GameState.RUNNING){
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            return;
        }
        
        player.setGameMode(GameMode.SURVIVAL);
        InventoryManager.clearInventory(player);
        InventoryManager.giveCombatKit(player);

        World world = game.getArenaWorld();
        Location respawnLoc;

        if (game.getBlackTeam().contains(player.getUniqueId())){
            respawnLoc = new Location (world, Game.BLACK_X, Game.BLACK_Y, Game.BLACK_Z, 0f, 0f);
        } else{
            respawnLoc = new Location (world, Game.GOLD_X, Game.GOLD_Y, Game.GOLD_Z, 180f, 0f);
        }
        player.teleport(respawnLoc);
        player.setGameMode(GameMode.SPECTATOR);
        Bukkit.getScheduler().runTaskLater(TurfWars.getInstance(), () -> {
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(respawnLoc);
            player.sendMessage("§aYou have respawned!");
        }, 100L); // 5-second respawn timer

    }

    // Ensures no PVP damage in the lobby area
    @EventHandler
    public void onLobbyDamage(EntityDamageByEntityEvent event){
        if (event.getEntity().getWorld().getName().equalsIgnoreCase("lobby")){
            event.setCancelled(true);
        }
    }

    // Restricts movement to only your team's side of the arena
    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event){
        Player player = event.getPlayer();
        Game game = findGame(player);

        // Only check if there is an active game
        if (game == null || game.getState() != GameState.RUNNING){
            return;
        }

        double newZ = event.getTo().getZ();
        double divideLine = game.getDivideLine();

        // Check for black team movement
        if (game.getBlackTeam().contains(player.getUniqueId())){
            if (newZ > divideLine){
                event.setCancelled(true);
                player.sendMessage("§cYou cannot enter gold team's territory!");
            }
        }
        // Check for gold team movement
        else if (game.getGoldTeam().contains(player.getUniqueId())){
            if (newZ < divideLine){
                event.setCancelled(true);
                player.sendMessage("§cYou cannot enter black team's territory!");
            }
        }
    }

    // Restricts team killing
    @EventHandler
    public void onTeamDamage(EntityDamageByEntityEvent event){
        // Checks both entities are players
        if (!(event.getEntity() instanceof Player victim)){
            return;
        }

        Player attacker = null;
        // Melee damage
        if (event.getDamager() instanceof Player p){
            attacker = p;
        }
        // Arrow damage
        else if (event.getDamager() instanceof org.bukkit.entity.Arrow arrow 
            && arrow.getShooter() instanceof Player p){
            attacker = p;
        }

        if (attacker == null){
            return;
        }
        
        Game game = findGame(victim);
        if (game == null || game.getState() == GameState.RUNNING){
            return;
        }

        // Check if both attacker and victim are on gold team
        boolean victimIsGold = game.getGoldTeam().contains(victim.getUniqueId());
        boolean attackerIsGold = game.getGoldTeam().contains(attacker.getUniqueId());

        // Both true (gold team) both false (black team)
        if (victimIsGold == attackerIsGold){
            event.setCancelled(true);
            attacker.sendMessage("§cYou cannot hurt your teammates!");
        }
    }

    private Game findGame(Player player){
        return GAME_MANAGER.getGames().stream()
            .filter(g -> g.getPlayers().contains(player.getUniqueId()))
            .findFirst().orElse(null);
    } 

}
