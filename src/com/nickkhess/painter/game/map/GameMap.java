package com.nickkhess.painter.game.map;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import com.nickkhess.painter.utils.Cuboid;

public class GameMap {

	public static List<GameMap> gameMaps = new ArrayList<>();
	private String name;
	private Cuboid region;
	private Location lobbySpawn, gameSpawn;
	private int numberOfPlayers;
	private double knockbackFactor;

	private boolean inUse = false;

	public GameMap(String name, Location l1, Location l2, Location lobbySpawn, Location gameSpawn, int numberOfPlayers,
			double knockbackFactor) {
		this.name = name;
		region = new Cuboid(l1, l2);
		this.lobbySpawn = lobbySpawn;
		this.gameSpawn = gameSpawn;
		this.numberOfPlayers = numberOfPlayers;
		this.knockbackFactor = knockbackFactor;

		gameMaps.add(this);
	}

	public String getName() {
		return name;
	}

	public Cuboid getRegion() {
		return region;
	}

	public Location getLobbySpawn() {
		return lobbySpawn;
	}

	public Location getGameSpawn() {
		return gameSpawn;
	}

	public int getNumberOfPlayers() {
		return numberOfPlayers;
	}

	public boolean isInUse() {
		return inUse;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	public double getKnockbackFactor() {
		return knockbackFactor;
	}

	public static GameMap getByName(String name) {
		for(GameMap map : gameMaps)
			if(map.getName().equalsIgnoreCase(name))
				return map;
		return null;
	}

}