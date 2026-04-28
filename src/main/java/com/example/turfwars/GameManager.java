package com.example.turfwars;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameManager {

    private final Map<String, Game> games = new HashMap<>();
    private final TurfWars plugin;

    public GameManager(TurfWars plugin) {
        this.plugin = plugin;
    }

  
    public void setupGames() {
        createGame("game-1");
        createGame("game-2");
    }

    public void createGame(String name) {
        Game game = new Game(name);
        games.put(name, game);
    }

    public void endGame(String name, String message) {
        Game game = games.get(name);
        if (game != null) {
            game.endGame(message);
        }
    }

    public void restartGame(String name){
        games.remove(name);
        createGame(name);

        TurfWars.getInstance().getLogger().info("Game " + name + " has been reset and is ready for new players.");
    }

    public Collection<Game> getGames() {
        return games.values();
    }

    public Game getGame(String name) {
        return games.get(name);
    }
}