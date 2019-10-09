package com.nickkhess.painter;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

import com.nickkhess.painter.game.Game;

public class EventManager implements Listener {

	static BukkitScheduler scheduler = Bukkit.getScheduler();

	public static HashMap<Player, Player> lastHitBy = new HashMap<Player, Player>();

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		final Player player = e.getPlayer();
		Location location = player.getLocation();
		Block block = location.getBlock();
		Block down = block.getRelative(BlockFace.DOWN);

		if(Game.isInGame(player)) {
			Game game = Game.getGame(player);

			if(player.getWorld().getName().equals("Painter")) {
				Material type = down.getType();
				if(Tag.WOOL.isTagged(type)) {
					// Verify that the block underneath the player is NOT already of their type
					if(!game.getPlayerBlockType(player).equals(type)) {
						// If the game hasn't started yet
						if(game.getPhase() == 0 || game.getPhase() == 1) {
							// Paint it but don't add score
							game.paint(player, false);
						}
						// If the game HAS started
						else if(game.getPhase() == 2) {
							// Paint it AND add score
							game.paint(player, true);
							//game.setScore(player, game.getScore(player) + 1);
						}
					}

					if(game.getPhase() == 0 || game.getPhase() == 1 || game.getPhase() == 2)
						// If the game is running, show particles to all players underneath each player
						for(Player showTo : Bukkit.getOnlinePlayers())
							showTo.spawnParticle(Particle.REDSTONE, player.getLocation(), 3, new DustOptions(game.getPlayerColor(player), 1));
				}
			}

			if(location.getY() <= 45 && player.getWorld().getName().equals("Painter")) {
				tpToCenter(player);
				if(lastHitBy.get(player) != null)
					Game.getGame(player).rainColors(lastHitBy.get(player), player);
			}
		}
		if(!e.getTo().getWorld().getName().equals("Painter") && Game.isInGame(player))
			Game.getGame(player).removePlayer(player, 0, false);
	}

	public void tpToCenter(Player p) {
		if(Game.isInGame(p)) {
			p.teleport(Game.getGame(p).getInGameSpawn());
			p.setFallDistance(0);
		}
	}

	@EventHandler
	public void onPlayerClickInventory(InventoryClickEvent e) {
		if(Game.isInGame((Player) e.getWhoClicked()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDamaged(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player) {
			if(e.getDamager() instanceof Player) {
				Player p = (Player) e.getDamager();
				Player t = (Player) e.getEntity();

				if(Game.isInGame(p) || Game.isInGame(t)) {
					Game g = null;
					if(Game.isInGame(p))
						g = Game.getGame(p);
					if(Game.isInGame(t))
						g = Game.getGame(t);

					if(g.getPhase() == 0 || g.getPhase() == 1 || g.getPhase() == 3)
						e.setCancelled(true);

					t.setHealth(20);
					t.setFallDistance(-1);
					lastHitBy.put(t, p);
				}
			}
			else if(e.getDamager() instanceof Silverfish)
				e.setCancelled(true);
		}
	}

	@EventHandler
	public void takeDamage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(Game.isInGame(p) && e.getCause().equals(DamageCause.FALL)) {

				Game g = Game.getGame(p);

				if(g.getPhase() == 2)
					g.splatterPaint(p);
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void hunger(FoodLevelChangeEvent e) {
		if(Game.isInGame((Player) e.getEntity()))
			e.setCancelled(true);
	}

	@EventHandler
	public void quit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if(Game.isInGame(p))
			Game.getGame(p).removePlayer(p, 0, false);
	}

	@EventHandler
	public void tp(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		String m = e.getMessage();

		if(m.equals("/hub") ||
				m.equals("/leave") ||
				m.equals("/lobby") ||
				m.equals("/spawn") ||
				m.toLowerCase().contains("/tp"))
			if(Game.isInGame(p))
				Game.getGame(p).removePlayer(p, 0, false);
	}

	@EventHandler
	public void breakBlock(BlockBreakEvent e) {
		if(Game.isInGame(e.getPlayer()))
			e.setCancelled(true);
	}

	@EventHandler
	public void placeBlock(BlockPlaceEvent e) {
		if(Game.isInGame(e.getPlayer()))
			e.setCancelled(true);
	}

}