package com.nickkhess.painter;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.plugin.java.JavaPlugin;
import com.nickkhess.painter.game.Game;
import com.nickkhess.painter.game.map.GameMap;

public class Painter extends JavaPlugin {

	public static HashMap<Silverfish, Player> launchedMobs = new HashMap<>();
	public static ArrayList<Player> players = new ArrayList<>();

	public static Painter plugin;

	@Override
	public void onEnable() {
		plugin = this;

		Bukkit.getPluginManager().registerEvents(new EventManager(), this);

		getCommand("painter").setExecutor(new CommandPainter());

		if(Game.getWorld() != null)
			Bukkit.getScheduler().runTaskTimer(this, (Runnable) () -> {
				for(Entity entity : Game.getWorld().getEntities())
					if(entity instanceof Silverfish && launchedMobs.containsKey(entity)
							&& entity.getWorld().equals(Game.getWorld())) {

						Player player = launchedMobs.get(entity);
						Game game = Game.getGame(player);

						if(entity.isOnGround()) {

							Block block = entity.getLocation().subtract(0, 1, 0).getBlock();

							while (!Tag.WOOL.isTagged(block.getType()))
								block = block.getLocation().subtract(0, 1, 0).getBlock();

							if(Tag.WOOL.isTagged(block.getType())) {
								game.setScore(player, game.getScore(player) + 1);
								entity.remove();
								block.setType(game.getPlayerBlockType(player));

								// Subtract score from player whose block it replaced
								if(game.getPlayerByBlockType(block.getType()) != null
										&& game.getPlayerByBlockType(block.getType()) != player)
									game.setScore(game.getPlayerByBlockType(block.getType()),
											game.getScore(game.getPlayerByBlockType(block.getType())) - 1);
							}

							launchedMobs.remove(entity);
						}
						else if(entity.getLocation().getY() <= 45)
							launchedMobs.remove(entity);

						for(Player showTo : Bukkit.getOnlinePlayers())
							showTo.spawnParticle(Particle.REDSTONE, entity.getLocation(), 3,
									new DustOptions(game.getPlayerColor(player), 1));
					}
			}, 0, 1);

		FileConfiguration config = Bukkit.getPluginManager().getPlugin("Painter").getConfig();
		if(config.get("maps") != null)
			for(String key : config.getConfigurationSection("maps").getKeys(false))
				new GameMap(key, (Location) config.get("maps." + key + ".point1"),
						(Location) config.get("maps." + key + ".point2"),
						(Location) config.get("maps." + key + ".lobbySpawn"),
						(Location) config.get("maps." + key + ".gameSpawn"),
						(int) config.get("maps." + key.toString() + ".numberOfPlayers"),
						(double) config.get("maps." + key.toString() + ".knockbackFactor"));
	}

	@Override
	public void onDisable() {

	}

}