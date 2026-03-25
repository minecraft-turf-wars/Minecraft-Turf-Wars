package com.example.turfwars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.GameMode;

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

    private double divideLine = 38.5;
    private final int FLOOR_Y = -61;
    private final int MIN_X = -43;
    private final int MAX_X = -1;
    private final int MIN_Z = 7;
    private final int MAX_Z = 70;
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
                    p.getInventory().addItem(new ItemStack(Material.GRAY_CONCRETE, 1));
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

        updateTerritoryBlocks();

        checkWinCondition();
    }

    public void updateTerritoryBlocks(){
        if (arenaWorld == null) return;
        for (int x = MIN_X; x <= MAX_X; x++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                Location loc = new Location(arenaWorld, x, FLOOR_Y, z);
                
                if (z < divideLine) {
                    loc.getBlock().setType(Material.BLACK_CONCRETE);
                } else {
                    loc.getBlock().setType(Material.YELLOW_CONCRETE);
                }
            }
        }
    }

    private void checkWinCondition(){
        if (divideLine >= MAX_Z || divideLine <= MIN_Z){
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
                p.getInventory().addItem(new ItemStack(Material.GRAY_CONCRETE, 7));
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

        this.divideLine = 38.5;
        updateTerritoryBlocks();
    }

    public void endGame() {
        if (this.arenaWorld != null) {
            ArenaManager.getInstance().unloadArena(this);
            this.state = GameState.ENDED;
        }
    }

    public void addPlayer(Player player) {
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
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

    public int getFloorY() {return FLOOR_Y;}

    public int getMinX() {return MIN_X;}

    public int getMaxX() {return MAX_X;}

    public int getMinZ() {return MIN_Z;}

    public int getMaxZ() {return MAX_Z;}

    public double getMoveStep() {return MOVE_STEP;}

    public World getArenaWorld() {return arenaWorld;}

    public String getName() {return name; }

    public GameState getState() {return state;}

    public void setState(GameState state) {this.state = state;}

    public List<UUID> getPlayers() {return players;}

    public int getMaxPlayers() {return maxPlayers;}
}
