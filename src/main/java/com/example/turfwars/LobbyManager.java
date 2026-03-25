package com.example.turfwars;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class LobbyManager {

    private static LobbyManager instance;
    private final String LOBBY_WORLD_NAME = "lobby";

    public LobbyManager() {
        instance = this;
        this.createLobby();
    }

    public static LobbyManager getInstance() {
        return instance;
    }

    public World createLobby() {
        File templateWorldDir = new File(TurfWars.getInstance().getDataFolder(), "lobby");
        File newWorldDir = new File(Bukkit.getWorldContainer(), LOBBY_WORLD_NAME);

        if (!templateWorldDir.exists()) {
            TurfWars.getInstance().getLogger().severe("Template world folder does not exist!");
            return null;
        }

        try {
            copyDirectory(templateWorldDir, newWorldDir);
            new File(newWorldDir, "uid.dat").delete(); // VERY IMPORTANT
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        World lobbyWorld = Bukkit.createWorld(new WorldCreator(LOBBY_WORLD_NAME));
        if (lobbyWorld != null) {
            lobbyWorld.setAutoSave(false);
            lobbyWorld.setGameRule(org.bukkit.GameRule.KEEP_INVENTORY, true);
            lobbyWorld.setGameRule(org.bukkit.GameRule.DO_IMMEDIATE_RESPAWN, true);
        }
        return lobbyWorld;
    }

    public void unloadArena(Game game) {
        World world = game.getArenaWorld();
        if (world != null) {
            // Teleport all players out of the world before unloading
            World mainWorld = Bukkit.getWorlds().get(0); // Assuming the main world is the first one
            for (Player player : world.getPlayers()) {
                player.teleport(mainWorld.getSpawnLocation());
            }

            // Unload the world
            boolean unloaded = Bukkit.unloadWorld(world, false);
            if (unloaded) {
                TurfWars.getInstance().getLogger().info("Successfully unloaded world: " + world.getName());
                // Delete the world folder
                try {
                    deleteDirectory(world.getWorldFolder());
                    TurfWars.getInstance().getLogger().info("Successfully deleted world folder: " + world.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                    TurfWars.getInstance().getLogger().severe("Failed to delete world folder: " + world.getName());
                }
            } else {
                TurfWars.getInstance().getLogger().severe("Failed to unload world: " + world.getName());
            }
        }
    }

    private void copyDirectory(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdir();
        }
        if (source.isDirectory()) {
            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(target, file);
                    copyDirectory(srcFile, destFile);
                }
            }
        } else {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }


    private void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        if (!directory.delete()) {
            throw new IOException("Failed to delete " + directory);
        }
    }

    public void sendToLobby(Player player) {
        World lobbyWorld = Bukkit.getWorld(LOBBY_WORLD_NAME);
        Location lobbySpawn = new Location (lobbyWorld, 2, -60, -21); 
        if (lobbyWorld == null) {
            player.sendMessage("§cError: Lobby world is not available.");
            return;
        }
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.teleport(lobbySpawn);
        player.sendMessage("§aWelcome to the TurfWars Lobby!");

    }
}






