package com.nickkhess.painter.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import com.nickkhess.painter.EventManager;
import com.nickkhess.painter.Painter;
import com.nickkhess.painter.events.PlayerPainterStateChangeEvent;
import com.nickkhess.painter.game.timers.GameTimer;
import com.nickkhess.painter.utils.Cuboid;

public class Game {
	
	private int id;

	private static World world = Bukkit.getWorld("Painter");

	private HashMap<String, String> teams = new HashMap<String, String>();

	private ArrayList<Player> players = new ArrayList<Player>();
	private static HashMap<Game, GameTimer> games = new HashMap<Game, GameTimer>();

	private final int defaultMaxPlayers = 4;
	private int maxPlayers = 4;

	private int phase = 0;

	private HashMap<OfflinePlayer, Location> preGameLocation = new HashMap<OfflinePlayer, Location>();
	private HashMap<OfflinePlayer, GameMode> preGameGM = new HashMap<OfflinePlayer, GameMode>();
	private HashMap<OfflinePlayer, Inventory> preGameInventory = new HashMap<OfflinePlayer, Inventory>();

	private HashMap<OfflinePlayer, Integer> bonus = new HashMap<OfflinePlayer, Integer>();

	private ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
	private Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
	private Objective objective = scoreboard.registerNewObjective("Painter", "dummy", "Painter");

	private HashMap<Player, Integer> scores = new HashMap<Player, Integer>();

	private FileConfiguration config = Painter.plugin.getConfig();
	private Cuboid region = new Cuboid((Location) config.get("point1"), (Location) config.get("point2"));

	private Player winner = null;

	private GameTimer timer;

	private Location preGameSpawn = (Location) config.get("preGameSpawn");
	private Location inGameSpawn = (Location) config.get("inGameSpawn");

	public Game(int offX, int offY, int offZ) {
		region = region.copy(offX, offY, offZ);
		preGameSpawn = preGameSpawn.add(offX, offY, offZ);

		GameTimer t = new GameTimer(this);
		games.put(this, t);
		timer = t;

		reset(true, true, true, true);
	}

	public String sendPlayerInStatusMessage(Player player, boolean join) { 
		ChatColor playerColor = ChatColor.RED;
		playerColor = getChatColor(player);

		int sub = 0;
		ChatColor messageColor = ChatColor.YELLOW;

		if(!join) {
			messageColor = ChatColor.RED;
			sub = 1;
		}
		return messageColor + "(" + playerColor + (players.size() - sub) + messageColor + "/" + playerColor + maxPlayers + messageColor + ")";
	}

	public void addPlayer(Player player) {
		if(!isInGame(player)) {
			if(players.size() < maxPlayers) {
				if(phase < 1) {
					if(players.size() == 0)
						for(Player p : players) {
							removeScore(p);
							removePlayer(p, 0, true);
						}
					players.add(player);
					Painter.players.add(player);

					randomTeam(player);

					player.setScoreboard(scoreboard);
					preGameLocation.put(player, player.getLocation());
					player.teleport(preGameSpawn);
					for(Player pl : players)
						pl.sendMessage(getChatColor(player) + player.getName() + ChatColor.YELLOW + " has joined! " + sendPlayerInStatusMessage(player, true));

					if(players.size() == maxPlayers)
						start();

					preGameGM.put(player, player.getGameMode());
					preGameInventory.put(player, player.getInventory());
					player.getInventory().clear();
					player.setGameMode(GameMode.ADVENTURE);
					player.setExp(0);
					player.setLevel(0);
					player.setHealth(20);
					player.setSaturation(20);

					ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
					ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
					ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
					ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

					LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
					LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
					LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
					LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();

					helmetMeta.setColor(getPlayerColor(player));
					chestplateMeta.setColor(getPlayerColor(player));
					leggingsMeta.setColor(getPlayerColor(player));
					bootsMeta.setColor(getPlayerColor(player));

					helmetMeta.setDisplayName(getChatColor(player) + player.getName() + "'s Helmet");
					chestplateMeta.setDisplayName(getChatColor(player) + player.getName() + "'s Chestplate");
					leggingsMeta.setDisplayName(getChatColor(player) + player.getName() + "'s Leggings");
					bootsMeta.setDisplayName(getChatColor(player) + player.getName() + "'s Boots");

					helmet.setItemMeta(helmetMeta);
					chestplate.setItemMeta(chestplateMeta);
					leggings.setItemMeta(leggingsMeta);
					boots.setItemMeta(bootsMeta);

					player.getInventory().setArmorContents(new ItemStack[] {boots, leggings, chestplate, helmet});

					Bukkit.getPluginManager().callEvent(new PlayerPainterStateChangeEvent(player, id, true));
				}
				else if(phase != 0)
					player.sendMessage(ChatColor.RED + "That game is already running!");
			}
			else
				player.sendMessage(ChatColor.RED + "That game is full!");
		}
		else
			player.sendMessage(ChatColor.RED + "You are already in a game!");
	}

	public void removePlayer(Player player, int type, boolean tp) {
		if(type == 0) {
			if(!(phase > 2)) {
				boolean reset = false;
				if(phase == 1) {
					phase = 0;
					reset = true;
				}

				for(Player pl : players)
					if(pl != player)
						pl.sendMessage(getChatColor(player) + player.getName() + ChatColor.RED + " has left" + (reset ? " and the countdown was cancelled" : "") + "! " + sendPlayerInStatusMessage(player, false));

				player.sendMessage(getChatColor(player) + player.getName() + ChatColor.RED + " has left!");

				if(reset)
					reset(false, false, false, true);
			}
			else {
				if(!isSpectator(player)) 
					for(Player pl : players)
						pl.sendMessage(getChatColor(player) + player.getName() + ChatColor.RED + " pixelated into thin air!");
			}
		}
		else if(type == 1) {
			player.setGameMode(GameMode.CREATIVE);
			for(Player onlinePlayer : players)
				onlinePlayer.hidePlayer(Painter.plugin, player);
			player.sendMessage(ChatColor.GRAY + "You are now spectating! Type /hub to leave the game!");
			Painter.players.remove(player);
		}
		if(type == 0 || type == 2) {
			if(tp)
				player.teleport(preGameLocation.get(player));
			player.setGameMode(preGameGM.get(player));
			preGameLocation.remove(player);
			preGameGM.remove(player);
			player.getInventory().setContents(preGameInventory.get(player).getContents());
			preGameInventory.remove(player);
			player.setScoreboard(scoreboardManager.getNewScoreboard());
			player.setLevel(0);

			players.remove(player);
			Painter.players.remove(player);
			Bukkit.getPluginManager().callEvent(new PlayerPainterStateChangeEvent(player, id, false));
		}

		if(!getPlayerTeam(player).equals(null))
			teams.put(getPlayerTeam(player), "");
		EventManager.lastHitBy.remove(player);

	}

	public boolean isPlayer(Player p) {
		return players.contains(p) && !isSpectator(p);
	}

	public boolean isSpectator(Player p) {
		return p.getGameMode().equals(GameMode.CREATIVE);
	}

	public String getPlayerTeam(Player p) {
		for(String team : teams.keySet())
			if(teams.get(team).equals(p.getUniqueId().toString()))
				return team;
		return "None";
	}

	public boolean isFull() {
		return (players.size() == maxPlayers);
	}

	public void reset(boolean removePlayers, final boolean resetWool, final boolean resetTeams, final boolean resetTimer) {
		if(removePlayers)
			for(Player p : players) {
				removeScore(p);
				removePlayer(p, 0, true);
				p.setLevel(0);
			}

		if(resetTeams) {
			teams.put("RED", "");
			teams.put("BLUE", "");
			teams.put("YELLOW", "");
			teams.put("GREEN", "");
		}
		
		maxPlayers = defaultMaxPlayers;
		

		BukkitScheduler scheduler = Bukkit.getScheduler();
		scheduler.scheduleSyncDelayedTask(Painter.plugin, new Runnable() {
			@Override
			public void run() {

				if(resetWool)
					for(Block block : region.getBlocks())
						if(Tag.WOOL.isTagged(block.getType())) {
							block.setType(Material.WHITE_WOOL);
						}

				if(resetTimer)
					if(phase == 0) {
						timer.reset();
					}

				phase = 0;
			}
		}, 20L);
	}

	public ChatColor chatColor(String team) {
		switch(team) {
		case "RED": return ChatColor.RED;
		case "BLUE": return ChatColor.BLUE;
		case "YELLOW": return ChatColor.YELLOW;
		case "GREEN": return ChatColor.GREEN;
		}
		return ChatColor.WHITE;
	}

	public Color getPlayerColor(Player player) {
		return getTeamColor(getPlayerTeam(player));
	}

	public Color getTeamColor(String team) {
		switch(team) {
		case "RED": return Color.RED;
		case "BLUE": return Color.BLUE;
		case "YELLOW": return Color.YELLOW;
		case "GREEN": return Color.GREEN;
		}
		return Color.WHITE;
	}

	public ChatColor getChatColor(Player p) {
		return chatColor(getPlayerTeam(p));
	}

	private void randomTeam(Player p) {
		ArrayList<String> available = new ArrayList<String>();
		for(String t : teams.keySet())
			if(teams.get(t).equals(""))
				available.add(t);

		Random r = new Random();

		String t = available.get(r.nextInt(available.size()));

		teams.put(t, p.getUniqueId().toString());
	}

	@SuppressWarnings("deprecation")
	public void showScores(Player p) {

		int entryNum = 0;

		ChatColor c = getChatColor(p);

		LinkedHashMap<Player, Integer> scores = new LinkedHashMap<Player, Integer>();

		p.sendMessage(c + "-------------------------------------------------");
		p.sendMessage(c + "                              Painter");
		scores.putAll(this.scores);

		List<Entry<Player, Integer>> entries =
				new ArrayList<Map.Entry<Player, Integer>>(scores.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<Player, Integer>>() {
			public int compare(Map.Entry<Player, Integer> a, Map.Entry<Player, Integer> b){
				return b.getValue().compareTo(a.getValue());
			}
		});
		for(Entry<Player, Integer> entry : entries) {
			entryNum++;
			String place = (entryNum == 1 ? ChatColor.DARK_AQUA + "1st" : (entryNum == 2 ? ChatColor.AQUA + "2nd" : (entryNum == 3 ? ChatColor.BLUE + "3rd" : ChatColor.YELLOW + "4th"))) + ChatColor.WHITE + " - ";
			if(!(entryNum >= 4)) {
				p.sendMessage("              " + place + getChatColor(entry.getKey()) + entry.getKey().getName() + ChatColor.WHITE + " - " + entry.getValue());
				bonus.put(Bukkit.getOfflinePlayer(ChatColor.stripColor(p.getName())), (3 - entryNum) * 10);
			}
		}
		p.sendMessage(c + "-------------------------------------------------");

		awardTokens(p);
	}

	BukkitScheduler scheduler = Bukkit.getScheduler();

	private void awardTokens(final Player p) {
		scheduler.scheduleSyncDelayedTask(Painter.plugin, new Runnable() {

			@Override
			public void run() {
				int tokensEarned = ((int)(scores.get(p) / 10)) + bonus.get(p);

				p.sendMessage(getChatColor(p) + "-------------------------------------------------");
				p.sendMessage(ChatColor.GOLD + "You earned " + tokensEarned + " tokens!");
				p.sendMessage(getChatColor(p) + "-------------------------------------------------");

				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tokens " + p.getName() + " " +  tokensEarned);
			}

		}, 80);
	}

	public void firework(Player player) {
		Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();

		Random r = new Random();   

		int randomType = r.nextInt(5);
		Type type = Type.BALL;     

		switch(randomType) {
		case 0: type = Type.BALL;
		break;
		case 1: type = Type.BALL_LARGE;
		break;
		case 2: type = Type.BURST;
		break;
		case 3: type = Type.CREEPER;
		break;
		case 4: type = Type.STAR;
		break;
		}

		FireworkEffect effect = FireworkEffect.builder().withColor(getPlayerColor(player)).with(type).trail(r.nextBoolean()).build();

		fwm.addEffect(effect);

		fwm.setPower(1);

		fw.setFireworkMeta(fwm);  	
	}

	public static void addToAvailable(Player p) {
		if(!games.isEmpty())
			for(Game game : games.keySet()) {
				if(!game.isFull() && game.phase <= 1) {
					if(!game.players.contains(p)) {
						game.addPlayer(p);
						break;
					}
					else {
						createNewGame().addPlayer(p);
						break;
					}
				}
				else
					createNewGame().addPlayer(p);
			}
		else
			createNewGame().addPlayer(p);
	}

	public static Game createAndAdd(final Player p) {
		final Game g = createNewGame();

		Bukkit.getScheduler().scheduleSyncDelayedTask(Painter.plugin, new Runnable() {

			@Override
			public void run() {
				g.addPlayer(p);
			}

		}, 5);

		return g;
	}

	public static Game createNewGame() {
		Game game = new Game(games.size()*1000, 0, 0);
		GameTimer timer = games.get(game);
		timer.runTaskTimer(Painter.plugin, 0, 20);
		game.timer = timer;
		game.id = games.size() + 1;
		game.teams.put("RED", "");
		game.teams.put("BLUE", "");
		game.teams.put("YELLOW", "");
		game.teams.put("GREEN", "");

		return game;
	}

	public static boolean isInGame(Player p) {
		for(Game g : games.keySet())
			if(g.isPlayer(p))
				return true;

		return false;
	}

	public static Game getGame(Player player) {
		for(Game game : games.keySet())
			if(game.isPlayer(player))
				return game;

		return null;
	}

	public void setScore(Player player, int score) {
		scores.put(player, score);
		objective.getScore(getChatColor(player) + player.getName()).setScore(score);
	}

	public int getScore(Player player) {
		return scores.get(player);
	}

	public void removeScore(Player player) {
		scores.put(player, null);
		scoreboard.resetScores(getChatColor(player) + player.getName());
	}

	public Player getPlayerByScore(int score) {
		for(Player player : scores.keySet())
			if(score == getScore(player))
				return player;

		return null;
	}

	public boolean hasHighestScore(Player p) {
		return Collections.max(scores.values()) == getScore(p);
	}

	public void start() {
		maxPlayers = players.size();
		phase = 1;
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(String.format("%sP%sA%sI%sN%sT%sE%sR", 
				ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.AQUA, ChatColor.BLUE, ChatColor.LIGHT_PURPLE));
		for(Player pl : players) {
			Score score = objective.getScore(getChatColor(pl) + pl.getName());
			score.setScore(0);
		}
	}

	public void paint(Player player, boolean addscore) {
		if(Game.isInGame(player)) {
			Game game = Game.getGame(player);
			if(!game.isSpectator(player)) {
				Block block = player.getLocation().subtract(0, 1, 0).getBlock();
				block.setType(game.getPlayerBlockType(player));
				if(addscore) {
					game.setScore(player, game.scores.get(player) + 1);
					for(Player gamePlayer : game.players)
						if(game.getPlayerBlockType(gamePlayer) == block.getType() && player != gamePlayer)
							game.setScore(gamePlayer, game.scores.get(gamePlayer) - 1);
					Sounds.playPaintSound(player);
				}
			}
		}
	}

	public void splatterPaint(Player p) {
		if(Game.isInGame(p)) {
			Game g = Game.getGame(p);
			if(!g.isSpectator(p))
				for(int i = 0; i < 8; i++)
					Painter.launchedMobs.put(launchMob(p.getLocation(), 1), p);
		}
	}

	public void rainColors(Player hitter, Player hit) {

		int num1 = ThreadLocalRandom.current().nextInt(7, 10);
		int num2 = new Random().nextInt(3);

		for(int i = 0; i < num1; i++)
			Painter.launchedMobs.put(launchMob(inGameSpawn, 3), hitter);

		for(int i = 0; i < num2; i++)
			Painter.launchedMobs.put(launchMob(inGameSpawn, 3), hit);
	}

	public int getID() {
		return id;
	}

	private Silverfish launchMob(Location l, int multiply) {
		Random r = new Random();
		float x = (r.nextFloat()*.5f);
		float y = (r.nextFloat()*.2f) + .4f;
		float z = (r.nextFloat()*.5f);

		Silverfish s = (Silverfish) world.spawnEntity(l.add(0, 1, 0), EntityType.SILVERFISH);

		s.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999, 2, false, true));

		s.setVelocity(new Vector(x, y, z));

		return s;
	}

	public Player getPlayerByBlockType(Material material) {
		if(material.equals(Material.RED_WOOL))
			return getPlayerByTeam("RED");
		else if(material.equals(Material.BLUE_WOOL))
			return getPlayerByTeam("BLUE");
		else if(material.equals(Material.YELLOW_WOOL))
			return getPlayerByTeam("YELLOW");
		else if(material.equals(Material.LIME_WOOL))
			return getPlayerByTeam("GREEN");
		return null;
	}

	public Player getPlayerByTeam(String team) {
		return Bukkit.getPlayer(teams.get(team));
	}

	public Material getPlayerBlockType(Player player) {
		if(getPlayerTeam(player).equals("RED"))
			return Material.RED_WOOL;
		else if(getPlayerTeam(player).equals("BLUE"))
			return Material.BLUE_WOOL;
		else if(getPlayerTeam(player).equals("YELLOW"))
			return Material.YELLOW_WOOL;
		else if(getPlayerTeam(player).equals("GREEN"))
			return Material.LIME_WOOL;
		else
			return null;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public HashMap<Player, Integer> getScores() {
		return scores;
	}

	public Location getInGameSpawn() {
		return inGameSpawn;
	}

	public int getPhase() {
		return phase;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	public Player getWinner() {
		return winner;
	}

	public void setWinner(Player winner) {
		this.winner = winner;
	}

	public static World getWorld() {
		return world;
	}

	public static void setWorld(World world) {
		Game.world = world;
	}

	public static HashMap<Game, GameTimer> getGames() {
		return games;
	}

	public static void setGames(HashMap<Game, GameTimer> games) {
		Game.games = games;
	}

}