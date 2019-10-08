package com.nickkhess.painter.game;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.nickkhess.painter.Painter;

public class Sounds {

	public static void playPaintSound(Player p) {
		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_SPLASH, .05f, 2);
	}

	public static void tick(Player p) {
		p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 2, 2);
	}

	public static void start(Player p) {
		p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2, .9f);
	}

	public static void finish(final Player p) {
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 2, 1.6f);
		
		 final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
	        scheduler.scheduleSyncDelayedTask(Painter.plugin, new Runnable() {
	            @Override
	            public void run() {
	            	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 2, 1.4f);
	            	scheduler.scheduleSyncDelayedTask(Painter.plugin, new Runnable() {
	    	            @Override
	    	            public void run() {
	    	            	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 2, 1f);
	    	            }
	    	        }, 1L);
	            }
	        }, 1L);
	}

	public static void scores(Player p) {
		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2, 1.1f);
	}

}