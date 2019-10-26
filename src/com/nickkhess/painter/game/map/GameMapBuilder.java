package com.nickkhess.painter.game.map;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.nickkhess.painter.exceptions.InvalidNumberOfPlayersException;

public class GameMapBuilder {

	public static Map<Player, GameMapBuilder> editingMap = new HashMap<>();

	private String name = "Unnamed";
	private Location l1, l2, lobbySpawn, gameSpawn = null;
	int numberOfPlayers = 4;
	double knockbackFactor = 1.0;

	public GameMapBuilder(GameMap gameMap) {
		name = gameMap.getName();
		l1 = gameMap.getRegion().getLowerNE();
		l2 = gameMap.getRegion().getUpperSW();
		lobbySpawn = gameMap.getLobbySpawn();
		gameSpawn = gameMap.getGameSpawn();
		knockbackFactor = gameMap.getKnockbackFactor();
	}

	public GameMapBuilder() {
	}

	public GameMapBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public GameMapBuilder setRegionPoint1(Location l1) {
		this.l1 = l1;
		return this;
	}

	public GameMapBuilder setRegionPoint2(Location l2) {
		this.l2 = l2;
		return this;
	}

	public GameMapBuilder setLobbySpawn(Location lobbySpawn) {
		this.lobbySpawn = lobbySpawn;
		return this;
	}

	public GameMapBuilder setGameSpawn(Location gameSpawn) {
		this.gameSpawn = gameSpawn;
		return this;
	}

	public GameMapBuilder setNumberOfPlayers(int numberOfPlayers) throws InvalidNumberOfPlayersException {
		if(numberOfPlayers <= 16) {
			this.numberOfPlayers = numberOfPlayers;
			return this;
		}
		else
			throw new InvalidNumberOfPlayersException();
	}

	public GameMapBuilder setKnockbackFactor(double knockbackFactor) {
		this.knockbackFactor = knockbackFactor;
		return this;
	}

	public GameMap build() {
		return new GameMap(name, l1, l2, lobbySpawn, gameSpawn, numberOfPlayers, knockbackFactor);
	}

	public String getName() {
		return name;
	}

}