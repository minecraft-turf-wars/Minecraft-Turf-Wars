package com.example.turfwars;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {

    private final String name;
    private GameState state;
    private final List<UUID> players;
    private final int maxPlayers = 16; // Example value

    private World arenaWorld;
    private final String worldName;

    public Game(String name) {
        this.name = name;
        this.worldName = "turfwars_arena_" + name;
        this.state = GameState.WAITING;
        this.players = new ArrayList<>();
    }

    public void startGame() {
        this.state = GameState.STARTING;
        this.arenaWorld = ArenaManager.getInstance().createArena(worldName);
        if (this.arenaWorld != null) {
            this.state = GameState.RUNNING;
        }
    }

    public void endGame() {
        if (this.arenaWorld != null) {
            ArenaManager.getInstance().unloadArena(this);
            this.state = GameState.ENDED;
        }
    }

    public World getArenaWorld() {
        return arenaWorld;
    }

    public String getName() {
        return name;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        if (arenaWorld != null) {
            Location spawnPoint = arenaWorld.getSpawnLocation();
            player.teleport(spawnPoint);
        } else {
            player.sendMessage("Arena world is not ready yet!");
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }
}
