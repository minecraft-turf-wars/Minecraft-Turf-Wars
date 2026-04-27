package com.example.turfwars;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Game {

    private final String name;
    private GameState state;
    private final List<UUID> players;
    private final int MIN_PLAYERS = 4;
    private final int MAX_PLAYERS = 16;
    private final Set<UUID> startVotes = new HashSet<>();
    private int countdownTaskID = -1;

    private World arenaWorld;
    private final String worldName;

    private double divideLine = 38.5;
    private final int FLOOR_Y = -61;
    private final int MIN_X = -43;
    private final int MAX_X = -1;
    private final int MIN_Z = 7;
    private final int MAX_Z = 70;

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

    // Start game mechanics
    public void startMechanics() {
        Bukkit.getScheduler().runTaskTimer(TurfWars.getInstance(), () -> {
            if (state != GameState.RUNNING) return;
            for (UUID uuid : players) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    Material teamBlock = BLACK_TEAM.contains(uuid) ? Material.BLACK_WOOL : Material.YELLOW_WOOL;
                    InventoryManager.giveReplenishItems(p, teamBlock);
                }
            }
        }, 0L, 100L);
    }
 
    // Kill mechanics (push or pull the divide line)
    public void handleKill(Player killer){
        
        // Hit marker sound
        killer.playSound(killer.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);
        
        // Dynamic moveStep counter (9+ players = 1 block, 5-8 players = 2 blocks, 4 or less players = 3 blocks)
        double moveStep;
        if(players.size() > 8){
            moveStep = 1.0;
        } else if(players.size() <= 8 && players.size() > 5){
            moveStep = 2.0;
        } else{
            moveStep = 3.0;
        }

        if (BLACK_TEAM.contains(killer.getUniqueId())){
            divideLine += moveStep;
        } else{
            divideLine -= moveStep;
        }

        updateTerritoryBlocks();

        checkWinCondition();
    }

    // Update the blocks in the arena depending on current divide line
    public void updateTerritoryBlocks(){
        if (arenaWorld == null) return;

        int MAX_Y = FLOOR_Y + 20;
        for (int x = MIN_X; x <= MAX_X; x++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {

                boolean isBlackTerritory = (z < divideLine);
                Location floorLoc = new Location(arenaWorld, x, FLOOR_Y, z);
                
                if (isBlackTerritory) {
                    floorLoc.getBlock().setType(Material.BLACK_CONCRETE);
                } else {
                    floorLoc.getBlock().setType(Material.YELLOW_CONCRETE);
                }

                // Destroys miscolored blocks after the update of the divide line
                for(int y = FLOOR_Y + 1; y <= MAX_Y; y++){
                    org.bukkit.block.Block block = arenaWorld.getBlockAt(x, y, z);
                    Material type = block.getType();

                    if(type==Material.AIR) continue;

                    if(isBlackTerritory){
                        if(type == Material.YELLOW_WOOL){
                            block.setType(Material.AIR);
                        } 
                    } else{
                        if(type == Material.BLACK_WOOL){
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    // Check to see if one team has controlled the entire arena (win condition)
    private void checkWinCondition(){
        if (divideLine >= MAX_Z ){
            endGame("§8BLACK TEAM §fWINS!");
        }
        else if (divideLine <= MIN_Z){
            endGame("§6GOLD TEAM §fWINS!");
        }
    }


    // Start game logic
    public void startGame() {
        this.arenaWorld = ArenaManager.getInstance().createArena(worldName);

        if (this.arenaWorld == null) return;

        this.state = GameState.RUNNING;

        BLACK_TEAM.clear();
        GOLD_TEAM.clear();

        Location blackSpawn = new Location(arenaWorld, BLACK_X, BLACK_Y, BLACK_Z, 0f, 0f);
        Location goldSpawn = new Location(arenaWorld, GOLD_X, GOLD_Y, GOLD_Z, 180f, 0f);

        for (int i = 0; i < players.size(); i++) {
            UUID uuid = players.get(i);
            Player p = Bukkit.getPlayer(uuid);

            Material teamBlock = (i % 2 == 0) ? Material.BLACK_WOOL : Material.YELLOW_WOOL;

            if (p != null){
                p.setGameMode(org.bukkit.GameMode.SURVIVAL);
                InventoryManager.clearInventory(p);
                InventoryManager.resetHealtAndHunger(p);
                InventoryManager.giveCombatKit(p, teamBlock);
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

    // Ends the current game and restarts the game so it can be joined again
    public void endGame(String message) {
        if (this.state == GameState.ENDED){
            return;
        }
        this.state = GameState.ENDED;
        this.startVotes.clear();

        // Clears inventory at end of game
        for (UUID uuid : players){
            Player p = Bukkit.getPlayer(uuid);
            if (p != null){
                p.sendTitle(message,"§7The game has ended.", 10, 70, 20);
                InventoryManager.clearInventory(p);
            }
        }
        if (this.arenaWorld != null){
            ArenaManager.getInstance().unloadArena(this);
        }

        this.players.clear();
        // Schedule the restart to happen on the next tick to avoid 
        // logic collisions during the world unloading process
        Bukkit.getScheduler().runTask(TurfWars.getInstance(), () -> {
            TurfWars.getInstance().getGameManager().restartGame(this.name);
        });
    }

    public void addPlayer(Player player) {
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
        }

        LobbyManager.getInstance().sendToLobby(player);

        broadcast("§a" + player.getName() + " joined the game! (" + players.size() + "/" + MAX_PLAYERS + ")");

        if(players.size() == MAX_PLAYERS && state == GameState.WAITING){
            startCountdown();
        } else if(players.size() == MIN_PLAYERS && state == GameState.WAITING){
            broadcast("§eMinimum players reached! Type §b/turfwars start §eto vote to start early.");
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        startVotes.remove(player.getUniqueId());

        broadcast("§c" + player.getName() + " left the game. (" + players.size() + "/" + MAX_PLAYERS + ")");

        if (player.isOnline()) {
            InventoryManager.clearInventory(player);
            InventoryManager.resetHealtAndHunger(player);
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
            
            World mainWorld = Bukkit.getWorlds().get(0); 
            player.teleport(mainWorld.getSpawnLocation());
        }

        if(state == GameState.STARTING && players.size() < MIN_PLAYERS) {
            cancelCountdown();
            broadcast("§cNot enough players to start. Countdown cancelled.");
        }
    }

    public void addStartVote(Player player){
        if(state != GameState.WAITING){
            player.sendMessage("§cThe game is already starting or running!");
            return;
        }
        if(players.size() < MIN_PLAYERS){
            player.sendMessage("§cCannot start yet. Need at least " + MIN_PLAYERS + " players.");
            return;
        }
        if(startVotes.contains(player.getUniqueId())){
            player.sendMessage("§cYou have already voted to start!");
            return;
        }

        startVotes.add(player.getUniqueId());
        int requiredVotes = getRequiredVotes();

        broadcast("§a" + player.getName() + " voted to start! (" + startVotes.size() + "/" + requiredVotes + ")");
        if(startVotes.size() >= requiredVotes){
            startCountdown();
        }
    }

    public void startCountdown(){
        if(state != GameState.WAITING){return;}
        this.state = GameState.STARTING;

        broadcast("§6Game is starting in 10 seconds!");

        countdownTaskID = Bukkit.getScheduler().scheduleSyncDelayedTask(TurfWars.getInstance(), () ->{
            if(state == GameState.STARTING){
                startGame();    
            }
        }, 200L);
    }

    public void cancelCountdown(){
        if(countdownTaskID != -1){
            Bukkit.getScheduler().cancelTask(countdownTaskID);
        }
        this.state = GameState.WAITING;
        this.startVotes.clear();
    }

    // Helper method for sending messages to all players in the game
    public void broadcast(String message){
        for(UUID uuid : players){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                p.sendMessage(message);
            }
        }
    }

    // Getters
    private int getRequiredVotes(){return (players.size() / 2) + 1;}

    public double getDivideLine() {return divideLine;}

    public List<UUID> getBlackTeam() {return BLACK_TEAM;}

    public List<UUID> getGoldTeam() {return GOLD_TEAM;}

    public int getFloorY() {return FLOOR_Y;}

    public int getMinX() {return MIN_X;}

    public int getMaxX() {return MAX_X;}

    public int getMinZ() {return MIN_Z;}

    public int getMaxZ() {return MAX_Z;}

    public World getArenaWorld() {return arenaWorld;}

    public String getName() {return name; }

    public GameState getState() {return state;}

    public void setState(GameState state) {this.state = state;}

    public List<UUID> getPlayers() {return players;}

    public int getMaxPlayers() {return MAX_PLAYERS;}
}
