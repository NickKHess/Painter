package com.nickkhess.painter.game.timers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.nickkhess.painter.game.Game;
import com.nickkhess.painter.game.Sounds;

public class GameTimer extends BukkitRunnable {

	private ChatColor yellow = ChatColor.YELLOW;

	private Game game = null;

	final int initialCountdownTime = 10;
	final int initialGameTime = 120;
	final int initialFinaleTime = 14;

	public int countdownTime = initialCountdownTime;
	public int gameTime = initialGameTime;
	public int finaleTime = initialFinaleTime;

	public GameTimer(Game game) {
		this.game = game;
	}

	@Override
	public void run() {
		if(game.getPhase() == 1) {
			if(countdownTime > 0) {
				for(Player player : game.getPlayers()) {
					player.sendMessage(yellow + "There" + (countdownTime == 1 ? " is " : " are ")
							+ game.getChatColor(player) + countdownTime + yellow
							+ (countdownTime == 1 ? " second" : " seconds") + " until the game starts!");
					Sounds.tick(player);
				}
				countdownTime--;
			}
			else {
				for(Player player : game.getPlayers()) {
					player.sendMessage(yellow + "The game has begun! Your color is " + game.getChatColor(player)
							+ game.getPlayerTeam(player) + yellow + "!");
					Sounds.start(player);
					game.getScores().put(player, 0);
					player.teleport(game.getGameSpawn());
				}
				countdownTime = initialCountdownTime;
				game.setPhase(2);
			}
			for(Player p : game.getPlayers())
				p.setLevel(countdownTime + 1);
		}
		else if(game.getPhase() == 2) {
			if(gameTime > 0) {
				if(gameTime % 60 == 0)
					for(Player player : game.getPlayers()) {
						player.sendMessage(yellow + "There " + (gameTime == 60 ? "is " : "are ")
								+ game.getChatColor(player) + gameTime / 60 + yellow
								+ (gameTime == 1 ? " minute" : " minutes") + " remaining!");
						Sounds.tick(player);
					}
				gameTime--;
			}
			else {
				gameTime = initialGameTime;
				game.setPhase(3);
				for(Player player : game.getPlayers())
					if(game.hasHighestScore(player))
						game.setWinner(player);
			}
			for(Player player : game.getPlayers())
				player.setLevel(gameTime + 1);
		}
		else if(game.getPhase() == 3)
			if(finaleTime > 0) {
				for(Player player : game.getPlayers())
					if(game.getWinner() != null)
						if(game.getWinner() == player)
							game.firework(player);
				if(finaleTime == initialFinaleTime)
					for(Player player : game.getPlayers()) {
						Sounds.scores(player);
						game.showScores(player);
						player.setLevel(0);
						player.getInventory().setArmorContents(null);
						if(game.hasHighestScore(player))
							game.setWinner(player);
					}
				finaleTime--;
			}
			else {
				for(Player player : game.getPlayers())
					game.removePlayer(player, 2, true);
				game.reset(true, true, true, true);
				finaleTime = initialFinaleTime;
			}
	}

	public void reset() {
		countdownTime = initialCountdownTime;
		gameTime = initialGameTime;
		finaleTime = initialFinaleTime;
	}

}