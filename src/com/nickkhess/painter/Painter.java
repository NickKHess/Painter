package com.nickkhess.painter;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.plugin.java.JavaPlugin;

import com.nickkhess.painter.game.Game;

public class Painter extends JavaPlugin {

	public static HashMap<Silverfish, Player> launchedMobs = new HashMap<Silverfish, Player>();
	public static ArrayList<Player> players = new ArrayList<Player>();

	public static Painter plugin;

	public void onEnable() {
		plugin = this;

		Bukkit.getPluginManager().registerEvents(new EventManager(), this);

		getCommand("painter").setExecutor(new CommandPainter());

		if(Game.getWorld() != null)
			Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
				@Override
				public void run() {
					for(Entity entity : Game.getWorld().getEntities()) {
						if(entity instanceof Silverfish && launchedMobs.containsKey(entity) && entity.getWorld().equals(Game.getWorld())) {

							Player player = launchedMobs.get(entity);
							Game game = Game.getGame(player);

							if(entity.isOnGround()) {

								Block block = entity.getLocation().subtract(0, 2, 0).getBlock();

								if(Tag.WOOL.isTagged(block.getType())) {
									game.setScore(player, game.getScore(player) + 1);
									if(game.getPlayerByBlockMaterial(block.getType()) != null)
										game.setScore(player, game.getScore(player) + 1);
								}

								entity.remove();
								block.setType(game.getPlayerBlockType(player));	

								launchedMobs.remove(entity);
							}
							else if(entity.getLocation().getY() <= 45)
								launchedMobs.remove(entity);


							for(Player showTo : Bukkit.getOnlinePlayers())
								showTo.spawnParticle(Particle.REDSTONE, entity.getLocation(), 3, new DustOptions(game.getPlayerColor(player), 1));
						}
					}
				}

			}, 0, 1);
	}

	public void onDisable() {

	}

}