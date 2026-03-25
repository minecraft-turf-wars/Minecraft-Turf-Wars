package com.example.turfwars;

<<<<<<< Updated upstream
=======
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.GameMode;

>>>>>>> Stashed changes
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Game {

    private final String name;
    private GameState state;
    private final List<UUID> players;
    private final int maxPlayers = 16; // Example value

    private World arenaWorld;
    private final String worldName;

    private double divideLine = 0;
    private final double MIN_BOUND = -50;
    private final double MAX_BOUND = 50;
    private final double MOVE_STEP = 1.0;

    private final List<UUID> BLACK_TEAM = new ArrayList<>();
    private final List<UUID> GOLD_TEAM = new ArrayList<>();

    public static final double BLACK_X = -21,  BLACK_Y = -59, BLACK_Z = 0;
    public static final double GOLD_X = -21, GOLD_Y = -59, GOLD_Z = 77;

    public Game(String name) {
        this.name = name;
        this.worldName = "turfwars_arena_" + name;
        this.state = GameState.WAITING;
        this.players = new ArrayList<>();
    }


    public void startMechanics() {
        Bukkit.getScheduler().runTaskTimer(TurfWars.getInstance(), () -> {
            if (state != GameState.RUNNING) return;
            for (UUID uuid : players) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.getInventory().addItem(new ItemStack(Material.OAK_PLANKS, 1));
                    p.getInventory().addItem(new ItemStack(Material.ARROW, 1));
                }
            }
        }, 0L, 100L);
    }

    public void handleKill(Player killer){
        if (BLACK_TEAM.contains(killer.getUniqueId())){
            divideLine += MOVE_STEP;
        } else{
            divideLine -= MOVE_STEP;
        }
        checkWinCondition();
    }

    private void checkWinCondition(){
        if (divideLine >= MAX_BOUND || divideLine <= MIN_BOUND){
            endGame();
        }
    }

    public void startGame() {
        this.arenaWorld = ArenaManager.getInstance().createArena(worldName);

        if (this.arenaWorld == null) return;

        this.state = GameState.RUNNING;

        BLACK_TEAM.clear();
        GOLD_TEAM.clear();

        Location blackSpawn = new Location(arenaWorld, BLACK_X, BLACK_Y, BLACK_Z);
        Location goldSpawn = new Location(arenaWorld, GOLD_X, GOLD_Y, GOLD_Z);

        for (int i = 0; i < players.size(); i++) {
            UUID uuid = players.get(i);
            Player p = Bukkit.getPlayer(uuid);

            if (p != null){
                p.getInventory().clear();
                p.getInventory().setArmorContents(null);
                p.setGameMode(org.bukkit.GameMode.SURVIVAL);
                p.setHealth(20.0);
                p.setFoodLevel(20);
                p.getInventory().addItem(new ItemStack(Material.ARROW, 7));
                p.getInventory().addItem(new ItemStack(Material.OAK_PLANKS, 7));
                p.getInventory().addItem(new ItemStack(Material.BOW, 1));
            }
            
            if (i % 2 == 0) {
                getBlackTeam().add(uuid);
                if (p != null) p.teleport(blackSpawn);
            } else {
                getGoldTeam().add(uuid);
                if (p != null) p.teleport(goldSpawn);
            }
            
            if (p != null) {
                String teamName = BLACK_TEAM.contains(uuid) ? "§8BLACK" : "§6GOLD";
                p.sendMessage("§fThe game has started! You are on the " + teamName + " §fteam.");
            }
        }

        startMechanics();
    }

    public void endGame() {
        if (this.arenaWorld != null) {
            ArenaManager.getInstance().unloadArena(this);
            this.state = GameState.ENDED;
        }
    }

    public void addPlayer(Player player) {
<<<<<<< Updated upstream
        if (arenaWorld != null) {
            players.add(player.getUniqueId());
            Location spawnPoint = arenaWorld.getSpawnLocation();
            player.teleport(spawnPoint);
        } else {
            player.sendMessage("Arena world is not ready yet!");
=======

        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
>>>>>>> Stashed changes
        }

        LobbyManager.getInstance().sendToLobby(player);
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

        // Getters
    public double getDivideLine() {return divideLine;}

    public List<UUID> getBlackTeam() {return BLACK_TEAM;}

    public List<UUID> getGoldTeam() {return GOLD_TEAM;}

    public double getMaxBound() {return MAX_BOUND;}

    public double getMinBound() {return MIN_BOUND;}

    public World getArenaWorld() {return arenaWorld;}

    public String getName() {return name; }

    public GameState getState() {return state;}

    public void setState(GameState state) {this.state = state;}

    public List<UUID> getPlayers() {return players;}

    public int getMaxPlayers() {return maxPlayers;}
}
