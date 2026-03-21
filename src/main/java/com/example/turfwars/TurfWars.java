package com.example.turfwars;

import com.example.turfwars.commands.TurfWarsCommand;
import com.example.turfwars.events.PlayerJoin;
import org.bukkit.plugin.java.JavaPlugin;

public class TurfWars extends JavaPlugin {

    private GameManager gameManager;
    private ArenaManager arenaManager;
    private LobbyManager lobbyManager;
    private TeamManager teamManager;

    @Override
    public void onEnable() {
        this.gameManager = new GameManager(this);
        this.arenaManager = new ArenaManager(this);
        this.lobbyManager = new LobbyManager(this);
        this.teamManager = new TeamManager(this);

        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getCommand("turfwars").setExecutor(new TurfWarsCommand());

        getLogger().info("TurfWars has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("TurfWars has been disabled!");
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }
}
