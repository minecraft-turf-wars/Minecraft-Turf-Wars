package com.example.turfwars;

import org.bukkit.event.Listener;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.entity.PlayerDeathEvent;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;

import io.papermc.paper.event.block.BlockBreakBlockEvent;

public class GameListener implements Listener{
    private final GameManager GAME_MANAGER;
    private final Map<UUID, Location> deathLocations = new HashMap<>();
    private final Set<UUID> spawnProtectedPlayers = new HashSet<>();

    public GameListener(GameManager GAME_MANAGER){
        this.GAME_MANAGER = GAME_MANAGER;
    }

    // Handles one-shot logic for arrow hits (and spawn protection negating damage)
    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent event){
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player killer)) return;

        Game game = findGame(victim);
        if(game == null || game.getState() != GameState.RUNNING) return;

        if(spawnProtectedPlayers.contains(victim.getUniqueId())){
            event.setCancelled(true);
            killer.sendMessage("§c" + victim.getName() + " currently has spawn protection!");
            killer.playSound(killer.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            arrow.remove();
            return;
        }

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
        Material teamBlock = game.getBlackTeam().contains(player.getUniqueId()) ? Material.BLACK_WOOL : Material.YELLOW_WOOL;
        InventoryManager.giveCombatKit(player, teamBlock);

        World world = game.getArenaWorld();
        Location respawnLoc;

        if (game.getBlackTeam().contains(player.getUniqueId())){
            respawnLoc = new Location (world, Game.BLACK_X, Game.BLACK_Y, Game.BLACK_Z, 0f, 0f);
        } else{
            respawnLoc = new Location (world, Game.GOLD_X, Game.GOLD_Y, Game.GOLD_Z, 180f, 0f);
        }

        Location deathLoc = deathLocations.getOrDefault(player.getUniqueId(), respawnLoc);

        player.teleport(deathLoc);
        player.setGameMode(GameMode.SPECTATOR);
        Bukkit.getScheduler().runTaskLater(TurfWars.getInstance(), () -> {
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(respawnLoc);
            player.sendMessage("§aYou have respawned!");

            deathLocations.remove(player.getUniqueId());

            spawnProtectedPlayers.add(player.getUniqueId());
            player.sendMessage("§bYou have spawn protection for 3 seconds!");

            Bukkit.getScheduler().runTaskLater(TurfWars.getInstance(), ()-> {
                if(spawnProtectedPlayers.remove(player.getUniqueId())){
                    if(player.isOnline()){
                        player.sendMessage("§cYour spawn protection has worn off!");
                    }
                }
            }, 60L); // 3-second respawn invincibility

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

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        double newZ = event.getTo().getZ();
        double divideLine = game.getDivideLine();

        // Check for black team movement
        if (game.getBlackTeam().contains(player.getUniqueId())){
            if (newZ > divideLine){
                Location pushBack = event.getFrom().clone();
                pushBack.setZ(pushBack.getZ() - 1.0);
                event.setTo(pushBack);
                player.sendMessage("§cYou cannot enter gold team's territory!");
            }
        }
        // Check for gold team movement
        else if (game.getGoldTeam().contains(player.getUniqueId())){
            if (newZ < divideLine){
                Location pushBack = event.getFrom().clone();
                pushBack.setZ(pushBack.getZ() + 1.0);
                event.setTo(pushBack);
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

    // Destorys player placed blocks with arrow hits
    // Ensures all arrows that hit in the arena are removed from the ground
    @EventHandler
    public void onArrowHitGround(ProjectileHitEvent event){
        if(!(event.getEntity() instanceof Arrow arrow)){
            return;
        }
        if(!(arrow.getShooter() instanceof Player shooter)){
            return;
        }
        Game game = findGame(shooter);
        if(game == null || game.getState() != GameState.RUNNING){
            return;
        }
        if (event.getHitBlock() != null) {
            org.bukkit.block.Block hitBlock = event.getHitBlock();
            Material blockType = hitBlock.getType();

        Bukkit.getScheduler().runTaskLater(TurfWars.getInstance(), () -> {
                if (blockType == Material.BLACK_WOOL || blockType == Material.YELLOW_WOOL) {
                    hitBlock.setType(Material.AIR);
                }
                
                if (!arrow.isDead()) {
                    arrow.remove();
                }
            }, 1L);
        }
    }

    // Removes player from game if they d/c from the server
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        Game game = findGame(player);

        spawnProtectedPlayers.remove(player.getUniqueId());

        if(game != null){
            game.removePlayer(player);
        }
    }

    // Captures player death loc for respawning purposes
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getPlayer();
        Game game = findGame(player);

        if(game!= null && game.getState() == GameState.RUNNING){
            deathLocations.put(player.getUniqueId(), player.getLocation());
        }
    }

    // Prevents block breaking in the lobby and the game arena
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();

        if(player.getWorld().getName().equalsIgnoreCase("lobby")){
            event.setCancelled(true);
        }

        Game game = findGame(player);
        if(game!=null){
            event.setCancelled(true);
        }
    }


    private Game findGame(Player player){
        return GAME_MANAGER.getGames().stream()
            .filter(g -> g.getPlayers().contains(player.getUniqueId()))
            .findFirst().orElse(null);
    } 

}