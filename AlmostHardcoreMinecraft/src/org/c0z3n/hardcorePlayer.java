package org.c0z3n;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.entity.Player;

import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name="ahm_players")
public class hardcorePlayer {
	@Id
	private UUID id;
	
	@NotNull
	private String playerName;

	@NotNull
	private int lastSpawnId;
	
	@NotNull
	private int deaths;
	
	@NotNull
	private int nightsAlive;
	
	@NotNull
	private int recordNightsAlive;
	
    public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
	
	public void setLastSpawnId(int spawnId) {
		this.lastSpawnId = spawnId;
	}
	
	public int getLastSpawnId(){
		return lastSpawnId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	public int getDeaths() {
		return deaths;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	public int getNightsAlive() {
		return nightsAlive;
	}

	public void setNightsAlive(int nightsAlive) {
		this.nightsAlive = nightsAlive;
	}

	public int getRecordNightsAlive() {
		return recordNightsAlive;
	}

	public void setRecordNightsAlive(int recordNightsAlive) {
		this.recordNightsAlive = recordNightsAlive;
	}

    public void addNightAlive(){
    	this.setNightsAlive(this.nightsAlive + 1);
    	if(this.nightsAlive > this.recordNightsAlive){
    		this.setRecordNightsAlive(this.nightsAlive);
    	}
    }
    public void addDeath(){
    	this.setDeaths(this.deaths + 1);
    }

	public void initializeFromPlayer(Player p) {
    	this.nightsAlive = 0;
    	this.recordNightsAlive = 0;
    	this.id = p.getUniqueId();
        this.playerName = p.getName();
		
	}
	
}
