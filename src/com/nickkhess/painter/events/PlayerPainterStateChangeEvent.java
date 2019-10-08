package com.nickkhess.painter.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerPainterStateChangeEvent extends Event {

	static HandlerList handlers = new HandlerList();
	Player player;
	int id;
	boolean join;
	
	public PlayerPainterStateChangeEvent(Player p, int id, boolean join) {
		this.player = p;
		this.id = id;
		this.join = join;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Player getPlayer() {
		return player;
	}

	public int getId() {
		return id;
	}

	public boolean isJoining() {
		return join;
	}

}