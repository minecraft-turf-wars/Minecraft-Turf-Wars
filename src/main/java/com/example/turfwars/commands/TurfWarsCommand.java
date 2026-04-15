package com.example.turfwars.commands;

import com.example.turfwars.Game;
import com.example.turfwars.GameManager;
import com.example.turfwars.GameState;

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

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /turfwars <list|join|end>");
            return true;
        }

        String action = args[0].toLowerCase();
        String gameName = args[1];
        Game game = gameManager.getGame(gameName);

        switch (action) {
            case "join":
                handleJoinCommand(player, args);
                break;
            case "start":
                handleStartCommand(player, game);
                break;
            case "force_start":
                if(player.hasPermission("turfwars.admin")){
                    handleForceStartCommand(player, game);
                } else{
                    player.sendMessage(ChatColor.RED + "You do not have permission to force start a game.");
                }
                break;
            case "force_end":
                if(player.hasPermission("turfwars.admin")){
                    handleForceEndCommand(player, game);
                } else{
                    player.sendMessage(ChatColor.RED + "You do not have permission to force start a game.");
                break;
                }
            default:
                if(player.hasPermission("turfwars.admin")){
                    player.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /turfwars <join|start|force_start|force_end>");
                } else{player.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /turfwars <join|start>");}
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

    private void handleForceEndCommand(Player player, Game game) {
        String gameName = game.getName();
        if (game == null){
            player.sendMessage(ChatColor.RED + gameName + "Game not found.");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Ending game: " + gameName);
        gameManager.endGame(gameName, "§cGAME FORCE-ENDED");
    }

    private void handleStartCommand(Player player, Game game){
        if(game.getPlayers().contains(player.getUniqueId())){
            game.addStartVote(player);
            return;
        }

    }

    private void handleForceStartCommand(Player player, Game game){
        if(game == null){
            player.sendMessage(ChatColor.RED + "That game could not be found.");
            return;
        }
        if(game.getState() != GameState.WAITING){
            player.sendMessage(ChatColor.RED + "Game is already starting or running.");
            return;
        }
        if(game.getPlayers().size() < 1){
            player.sendMessage(ChatColor.RED + "You need at least one player to force start the game.");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Forcing countdown start for: " + game.getName());
        game.startCountdown();
    }
}
