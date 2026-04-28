package com.example.turfwars.commands;

import com.example.turfwars.Game;
import com.example.turfwars.GameManager;
import com.example.turfwars.GameState;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// Class contains all of the text command logic
public class TurfWarsCommand implements CommandExecutor {

    private final GameManager gameManager;

    public TurfWarsCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    // Main engine for command line handling
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /turfwars <join|start|list|info>");
            return true;
        }

        String action = args[0].toLowerCase();
        Game game = null;

        if (args.length >= 2) {
            game = gameManager.getGame(args[1]);
        }

        // checks for each legal arg and calls the corresponding method
        switch (action) {
            case "info":
            case "credits":
                handleInfoCommand(player);
                break;
            case "list":
                handleListCommand(player);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /turfwars join <game_name>");
                    return true;
                }
                handleJoinCommand(player, args);
                break;
            case "start":
                if (game == null) {
                    player.sendMessage(ChatColor.RED + "Game not found.");
                    return true;
                }
                handleStartCommand(player, game);
                break;
            case "force_start":
                if (player.hasPermission("turfwars.admin")) {
                    handleForceStartCommand(player, game);
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to force start a game.");
                }
                break;
            case "force_end":
                if (player.hasPermission("turfwars.admin")) {
                    handleForceEndCommand(player, game);
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to force end a game.");
                }
                break;
            default:
                if (player.hasPermission("turfwars.admin")) {
                    player.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /turfwars <join|start|force_start|force_end|list|info>");
                } else {
                    player.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /turfwars <join|start|list|info>");
                }
                break;
        }

        return true;
    }

    // "info" command logic - displays dev names and links GitHub
    private void handleInfoCommand(Player player){
        player.sendMessage("§8§m----------------------------------------");
        player.sendMessage("§b§lTURF WARS §7- Minigame Plugin");
        player.sendMessage(" ");
        player.sendMessage("§fDeveloped by: §aMatthew Pfleger §f& §aChase Holt §f& §aAndrew Burgos");
        player.sendMessage("§aJohn McDowell §f& §aHezekiah Holloman");
        player.sendMessage("§fSource Code: §ehttps://github.com/minecraft-turf-wars/minecraft-turf-wars");
        player.sendMessage("§8§m----------------------------------------");
    }

    // "list" command logic - displays the list of available games
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

    // "join" command logic - allows players to join available games
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

    // "start" command logic - tallies the vote to start votes for a given game
    private void handleStartCommand(Player player, Game game){
        if(game.getPlayers().contains(player.getUniqueId())){
            game.addStartVote(player);
            return;
        }

    }

    // "force_start" command logic - ADMIN ONLY force start for a given game
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

    // "force_end" command logic - ADMIN ONLY force end for a given game
    private void handleForceEndCommand(Player player, Game game) {
        String gameName = game.getName();
        if (game == null){
            player.sendMessage(ChatColor.RED + gameName + "Game not found.");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Ending game: " + gameName);
        gameManager.endGame(gameName, "§cGAME FORCE-ENDED");
    }
}
