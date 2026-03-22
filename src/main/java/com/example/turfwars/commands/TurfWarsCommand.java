package com.example.turfwars.commands;

import com.example.turfwars.Game;
import com.example.turfwars.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TurfWarsCommand implements CommandExecutor {

    private final GameManager gameManager;

    public TurfWarsCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /turfwars <list|join|end>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                handleListCommand(player);
                break;
            case "join":
                handleJoinCommand(player, args);
                break;
            case "end":
                handleEndCommand(player, args);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /turfwars <list|join|end>");
                break;
        }

        return true;
    }

    private void handleListCommand(Player player) {
        player.sendMessage(ChatColor.YELLOW + "--- Available Games ---");
        for (Game game : gameManager.getGames()) {
            player.sendMessage(ChatColor.AQUA + game.getName() +
                    ChatColor.GRAY + " [" +
                    ChatColor.WHITE + game.getPlayers().size() + "/" + game.getMaxPlayers() +
                    ChatColor.GRAY + "] - " +
                    ChatColor.GREEN + game.getState());
        }
    }

    private void handleJoinCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /turfwars join <game_name>");
            return;
        }
        String gameName = args[1];
        Game game = gameManager.getGame(gameName);
        if (game != null) {
            game.addPlayer(player);
            player.sendMessage("Joining game: " + game.getName());
        } else {
            player.sendMessage("Game not found.");
        }
    }

    private void handleEndCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /turfwars end <game_name>");
            return;
        }
        String gameName = args[1];
        Game game = gameManager.getGame(gameName);
        if (game != null) {
            player.sendMessage("Ending game: " + game.getName());
            gameManager.endGame(gameName);
        } else {
            player.sendMessage("Game not found.");
        }
    }
}
