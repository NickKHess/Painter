package com.nickkhess.painter;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nickkhess.painter.game.Game;

import net.md_5.bungee.api.ChatColor;

public class CommandPainter implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(sender instanceof Player) {
			Player player = (Player)sender;

			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("join")) {
					if(Painter.plugin.getConfig().get("point1") != null &&
							Painter.plugin.getConfig().get("point2") != null &&
							Painter.plugin.getConfig().get("inGameSpawn") != null &&
							Painter.plugin.getConfig().get("preGameSpawn") != null)
						Game.addToAvailable(player);
					else
						player.sendMessage(ChatColor.RED + "The game is unconfigured! Please have an admin configure it!");
				}

				else if(args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("quit")) {
					if(Game.getGame(player) != null)
						Game.getGame(player).removePlayer(player, 0, true);
					else
						player.sendMessage(ChatColor.RED + "You are not in a game!");
				}

				else if(args[0].equalsIgnoreCase("start")) {
					if(player.isOp()) {
						if(Game.isInGame(player))
							Game.getGame(player).start();
						else player.sendMessage(ChatColor.RED + "You are not in a game!");
					}
					else
						player.sendMessage(ChatColor.RED + "You must be opped to use this command!");
				}

				else
					player.sendMessage(ChatColor.RED + "Invalid usage!");
			}
			else if(args.length == 2) {
				if(args[0].equalsIgnoreCase("set")) {
					if(player.hasPermission("painter.admin")) {
						Location location = player.getLocation();
						if(args[1].equalsIgnoreCase("point1") || args[1].equalsIgnoreCase("point2") ||
								args[1].equalsIgnoreCase("inGameSpawn") || args[1].equalsIgnoreCase("preGameSpawn")) {
							Painter.plugin.getConfig().set(args[1], location);
							Painter.plugin.saveConfig();
							player.sendMessage(ChatColor.YELLOW + "Successfully set " + args[1] + " to (" + ChatColor.AQUA + location.getBlockX() +
								ChatColor.YELLOW + ", " + ChatColor.AQUA + location.getBlockY() + 
								ChatColor.YELLOW + ", " + ChatColor.AQUA + location.getBlockZ() +
								ChatColor.YELLOW + ")!");
						}
						else
							player.sendMessage(ChatColor.RED + "Invalid usage!");
					}
					else
						player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
				}
				else
					player.sendMessage(ChatColor.RED + "Invalid usage!");
			}
			else
				player.sendMessage(ChatColor.RED + "Invalid usage!");
		}
		return true;
	}
}
