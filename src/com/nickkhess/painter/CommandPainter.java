package com.nickkhess.painter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.nickkhess.painter.exceptions.InvalidNumberOfPlayersException;
import com.nickkhess.painter.exceptions.MapInUseException;
import com.nickkhess.painter.game.Game;
import com.nickkhess.painter.game.map.GameMap;
import com.nickkhess.painter.game.map.GameMapBuilder;
import net.md_5.bungee.api.ChatColor;

public class CommandPainter implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;

			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("join")) {
					if(!GameMap.gameMaps.isEmpty())
						Game.addToAvailable(player);
					else
						player.sendMessage(
								ChatColor.RED + "The game is unconfigured! Please have an admin configure it!");
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
						else
							player.sendMessage(ChatColor.RED + "You are not in a game!");
					}
					else
						player.sendMessage(ChatColor.RED + "You must be opped to use this command!");
				}

				else
					player.sendMessage(ChatColor.RED + "Invalid usage!");
			}
			else if(args.length == 2) {
				if(args[0].equalsIgnoreCase("join"))
					if(GameMap.getByName(args[1]) != null) {
						Game gameToJoin = null;

						for(Game game : Game.getGames().keySet())
							if(game.getGameMap().getName().equalsIgnoreCase(args[1]))
								if(!game.isFull() && (game.getPhase() == 0 || game.getPhase() == 1))
									gameToJoin = game;
								else
									player.sendMessage(ChatColor.RED + "That game has already started or is full!");
						if(gameToJoin != null)
							gameToJoin.addPlayer(player);
						else
							try {
								Game.createAndAdd(player, GameMap.getByName(args[1]));
							}
							catch(MapInUseException e) {
								player.sendMessage(ChatColor.RED + "That map is not available!");
							}
					}
					else
						player.sendMessage(ChatColor.RED + "That map does not exist!");
			}
			else if(args.length == 3) {
				if(args[0].equalsIgnoreCase("map")) {
					if(player.hasPermission("painter.admin")) {
						Plugin painter = Bukkit.getPluginManager().getPlugin("Painter");

						if(args[1].equalsIgnoreCase("create")) {
							GameMapBuilder.editingMap.put(player, new GameMapBuilder().setName(args[2]));
							player.sendMessage(ChatColor.YELLOW + "Created map " + ChatColor.AQUA + args[2]);
							player.sendMessage(ChatColor.YELLOW + "Now editing map " + ChatColor.AQUA + args[2]);
						}
						else if(args[1].equalsIgnoreCase("edit")) {
							GameMapBuilder.editingMap.put(player, new GameMapBuilder(GameMap.getByName(args[2])));
							player.sendMessage(ChatColor.YELLOW + "Now editing map " + ChatColor.AQUA + args[2]);
						}
						else if(args[1].equalsIgnoreCase("delete")) {
							GameMap.gameMaps.remove(GameMap.getByName(args[2]));
							painter.getConfig().set("maps." + args[2], null);
							player.sendMessage(ChatColor.YELLOW + "Deleted map " + ChatColor.AQUA + args[2]);
						}
						else if(args[1].equalsIgnoreCase("save")) {
							if(GameMapBuilder.editingMap.keySet().contains(player)) {
								GameMap map = GameMapBuilder.editingMap.get(player).build();
								painter.getConfig().set("maps." + map.getName() + ".point1",
										map.getRegion().getLowerNE());
								painter.getConfig().set("maps." + map.getName() + ".point2",
										map.getRegion().getUpperSW());
								painter.getConfig().set("maps." + map.getName() + ".lobbySpawn", map.getLobbySpawn());
								painter.getConfig().set("maps." + map.getName() + ".gameSpawn", map.getGameSpawn());
								painter.getConfig().set("maps." + map.getName() + ".numberOfPlayers",
										map.getNumberOfPlayers());
								painter.saveConfig();

								GameMapBuilder.editingMap.remove(player);
								player.sendMessage(ChatColor.YELLOW + "Saved map " + ChatColor.AQUA + args[2]
										+ ChatColor.YELLOW + ". To edit it, use /painter map edit " + args[2] + "!");
							}
							else
								player.sendMessage(ChatColor.RED + "You are not editing a map!");
						}
						else if(args[1].equalsIgnoreCase("cancel")) {
							GameMapBuilder.editingMap.remove(player);
							player.sendMessage(
									ChatColor.YELLOW + "Cancelled creation of map " + ChatColor.AQUA + args[2]);
						}
						else if(args[1].equalsIgnoreCase("set"))
							if(GameMapBuilder.editingMap.keySet().contains(player)) {
								Location location = player.getLocation();

								if(args[2].equalsIgnoreCase("point1") || args[2].equalsIgnoreCase("point2")
										|| args[2].equalsIgnoreCase("gameSpawn")
										|| args[2].equalsIgnoreCase("lobbySpawn") || args[2].equalsIgnoreCase("name")
										|| args[2].equalsIgnoreCase("numberOfPlayers")) {
									if(args[2].equalsIgnoreCase("point1"))
										GameMapBuilder.editingMap.get(player)
												.setRegionPoint1(location.subtract(0, 1, 0));
									else if(args[2].equalsIgnoreCase("point2"))
										GameMapBuilder.editingMap.get(player)
												.setRegionPoint2(location.subtract(0, 1, 0));
									else if(args[2].equalsIgnoreCase("gameSpawn"))
										GameMapBuilder.editingMap.get(player).setGameSpawn(location);
									else if(args[2].equalsIgnoreCase("lobbySpawn"))
										GameMapBuilder.editingMap.get(player).setLobbySpawn(location);
									else if(args[2].equalsIgnoreCase("name"))
										GameMapBuilder.editingMap.get(player).setName(args[2]);
									else if(args[2].equalsIgnoreCase("numberOfPlayers"))
										try {
											GameMapBuilder.editingMap.get(player)
													.setNumberOfPlayers(Integer.parseInt(args[2]));
										}
										catch(NumberFormatException | InvalidNumberOfPlayersException e) {
											player.sendMessage(ChatColor.RED
													+ "Number of players must be a positive whole number between 1 and 16!");
										}

									player.sendMessage(ChatColor.YELLOW + "Successfully set " + ChatColor.AQUA + args[2]
											+ ChatColor.YELLOW + " of map " + ChatColor.AQUA
											+ GameMapBuilder.editingMap.get(player).getName() + ChatColor.YELLOW
											+ " to (" + ChatColor.AQUA + location.getBlockX() + ChatColor.YELLOW + ", "
											+ ChatColor.AQUA + location.getBlockY() + ChatColor.YELLOW + ", "
											+ ChatColor.AQUA + location.getBlockZ() + ChatColor.YELLOW + ")!");
								}
								else
									player.sendMessage(ChatColor.RED + "Invalid usage!");
							}
							else
								player.sendMessage(ChatColor.RED + "You are not editing a map!");
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
