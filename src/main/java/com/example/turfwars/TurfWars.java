package com.example.turfwars;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

import com.example.turfwars.commands.TurfWarsCommand;

public class TurfWars extends JavaPlugin {

    private static TurfWars instance;
    private ArenaManager arenaManager;
    private LobbyManager lobbyManager;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save the template world
        saveResource("template_world/dummy.txt", true);
        new File(getDataFolder(), "template_world/dummy.txt").delete();


        // Initialize the Managers
        this.arenaManager = new ArenaManager();
        this.lobbyManager = new LobbyManager();
        this.gameManager = new GameManager(this);

        this.gameManager.setupGames();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new GameListener(this.gameManager), this);

        // Register commands
        getCommand("turfwars").setExecutor(new TurfWarsCommand(this.gameManager));

        getLogger().info("TurfWars has been enabled!");
    }

    @Override
    public void onDisable() {
        // Clean up any active games
        if (gameManager != null) {
            // Create a copy of the list of games to avoid ConcurrentModificationException
            for (Game game : new java.util.ArrayList<>(gameManager.getGames())) {
                gameManager.endGame(game.getName(), "§cGAME FORCE-ENDED");
            }
        }
        getLogger().info("TurfWars has been disabled!");
    }

    public static TurfWars getInstance() {return instance;}

    public ArenaManager getArenaManager() {return arenaManager;}

    public GameManager getGameManager() {return gameManager;}
}