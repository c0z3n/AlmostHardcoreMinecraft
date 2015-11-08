package org.c0z3n;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
	private double lastSpawnX;
	

	@NotNull
	private double lastSpawnY;
	

	@NotNull
	private double lastSpawnZ;

	@NotNull
	private int deaths;

	@NotNull
	private int daysAlive;
	
	@NotNull
	private int recordDaysAlive;
	
	public void setId(UUID id){
		this.id = id;
	}
	
	public UUID getId(){
		return id;
	}
	
	public String getPlayerName(){
		return playerName;
	}

	public Player getPlayer() {
        return Bukkit.getServer().getPlayer(id);
    }

    public void setPlayer(Player player) {
        this.id = player.getUniqueId();
        this.playerName = player.getName();
    }
    
    public int getDeaths(){
    	return deaths;
    }
    
    public void setDeaths(int deaths){
    	this.deaths = deaths;
    }
    
    public void addDeath(){
    	this.deaths = this.deaths + 1;
    }
    
    public Location getLastSpawn(){
    	return new Location(null, lastSpawnX, lastSpawnX, lastSpawnX);
    }
    
    public void setLastSpawn(Location newLastSpawn){
    	this.lastSpawnX = newLastSpawn.getX();
    	this.lastSpawnY = newLastSpawn.getY();
    	this.lastSpawnZ = newLastSpawn.getZ();
    }
    
    public int getDaysAlive(){
    	return daysAlive;
    }
    
    public int getRecordDaysAlive(){
    	return recordDaysAlive;
    }
    
    public void addDayAlive(){
    	this.daysAlive = this.daysAlive + 1;
    	if(this.daysAlive > this.recordDaysAlive){
    		this.recordDaysAlive = this.daysAlive;
    	}
    }
	
}
