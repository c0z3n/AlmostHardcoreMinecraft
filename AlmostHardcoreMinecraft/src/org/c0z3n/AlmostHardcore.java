package org.c0z3n;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.persistence.PersistenceException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AlmostHardcore extends JavaPlugin{	
	
	HashMap<UUID, Integer[]> spawnData = new HashMap<UUID, Integer[]>();
	com.avaje.ebean.EbeanServer db = this.getDatabase();
	@Override
	public void onEnable() {
		
		// some event handlers
		this.getServer().getPluginManager().registerEvents(new Listener(){

			@EventHandler
			public void onDie(PlayerDeathEvent e) {
				// triggered when any player dies
				Player player = e.getEntity();
				updateGlobalSpawnLocation(player);
				Integer spnum = getConfig().getInt("deathCount."+player.getName());
				getConfig().set("deathCount."+player.getName(), spnum + 1);
				player.getEnderChest().clear();
			    saveConfig();
			}
			@EventHandler
			public void onJoin(PlayerJoinEvent e) {
				// triggered when any player joins
				Player player = e.getPlayer();
				if((spawnData.get(player.getUniqueId()) == null) || (!player.hasPlayedBefore())){
					updatePlayerDeathData(player, getServer().getWorlds().get(0).getSpawnLocation());
				}
				getConfig().addDefault("deathCount."+player.getName(), 0);
			    saveConfig();
				
			}
			@EventHandler
			public void onSpawn(PlayerRespawnEvent e) {
				Player player = e.getPlayer();
				boolean newSpawn = true;
				Integer spawnLocation[] = {e.getRespawnLocation().getBlockX(), e.getRespawnLocation().getBlockX()};
				for (Integer v[] : spawnData.values()) {
				    if(spawnProximityChecker(spawnLocation, v)){
				    	newSpawn = false;
				    }
				}
				if(newSpawn){
					getServer().broadcastMessage(ChatColor.RED + e.getPlayer().getName() + " died and is respawning at a brand new spawn point!");
					getServer().broadcastMessage(ChatColor.RED + "There may be some lag as the server generates new map tiles");
				} 
				updatePlayerDeathData(player, e.getRespawnLocation());
			}
			
			
		}, this);

	    this.getConfig().addDefault("RandomSpawnWindowSize", 500000);
	    this.getConfig().addDefault("totalSpawnsGenerated", 0);
	    this.getConfig().addDefault("TrackingDataFile", "trackingdata.dat");
	    this.getConfig().options().copyDefaults(true);
	    this.getServer().setSpawnRadius(0);
	    loadSpawnData();
	    saveConfig();
	}
	
	@Override
	public void onDisable() {
		saveSpawnData();
		saveConfig();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		Player player = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("deaths") && sender instanceof Player){
			Integer spawns = getConfig().getInt("deathCount." + player.getName());
			Integer totalDeaths  = 0;
			Map<String, Object> alldeaths = getConfig().getConfigurationSection("deathCount").getValues(false);

			// count total deaths
			for (Map.Entry<String, Object> entry : alldeaths.entrySet()) {
				totalDeaths += (int) entry.getValue();
			}
			player.sendMessage(ChatColor.RED + "total deaths       : " + totalDeaths.toString());
			player.sendMessage(ChatColor.RED + "your deaths        : " + spawns.toString());
		}

		if (cmd.getName().equalsIgnoreCase("deathboard") && sender instanceof Player){
			Map<String, Object> alldeaths = getConfig().getConfigurationSection("deathCount").getValues(false);
			getServer().broadcastMessage(ChatColor.RED + "DEATH SCOREBOARD");
			getServer().broadcastMessage("");
			Integer totalDeaths = 0;
			
			// count total deaths and calculate whitespace
			for (Map.Entry<String, Object> entry : alldeaths.entrySet()) {
				Integer spaces = 16 - entry.getKey().length();
				String whitespace = "";
				for(int i = 0; i< spaces; i++){
					whitespace = whitespace + " ";
				}
				getServer().broadcastMessage(ChatColor.RED + entry.getKey() + whitespace + ": " + entry.getValue().toString());
				totalDeaths += (int) entry.getValue();
			}
			getServer().broadcastMessage("");
			Integer spawnsGenerated = this.getConfig().getInt("totalSpawnsGenerated");
			getServer().broadcastMessage(ChatColor.RED + "TOTAL DEATHS          : " + totalDeaths);
			getServer().broadcastMessage(ChatColor.RED + "SPAWNS GENERATED      : " + spawnsGenerated);
		}
		
		
		return false;
	}
	
	private void loadDatabase() {
		try {
			this.getDatabase().find(hardcorePlayer.class).findRowCount();
		}
		catch (PersistenceException ex) {
            System.out.println(getDescription().getName() + " is setting up database");
            installDDL();
        }
	}

	public int randCoord() {
		Random rnd = new Random();
		int randWindowSize = this.getConfig().getInt("RandomSpawnWindowSize");
		return rnd.nextInt(randWindowSize) - randWindowSize/2;	
	}
	
	public Block newRandomSurfaceBlock(World w) {	
		// picks a new 
		Location randLocation = new Location(w, randCoord(), 0, randCoord());
		return w.getHighestBlockAt(randLocation);
		
	}

	public void newRandomWorldSpawn(World w) {
		//rule out some unplayable biomes
		Biome[] badBiomes = {Biome.OCEAN, Biome.DEEP_OCEAN, Biome.FROZEN_OCEAN};
		boolean badBiome = true;
		Location newSpawn = null;
		while(badBiome == true){
			Block newCandidateSpawnBlock = newRandomSurfaceBlock(w);
			newSpawn = newCandidateSpawnBlock.getLocation();
			badBiome = Arrays.asList(badBiomes).contains(newCandidateSpawnBlock.getBiome());
		}
		w.setSpawnLocation(newSpawn.getBlockX(), newSpawn.getBlockY(), newSpawn.getBlockZ());
	}
	
	public boolean spawnProximityChecker(Integer a1[], Integer a2[]){
		Arrays.sort(a1);
		Arrays.sort(a2);
		Integer th = 25; //threshold - minecraft doesn't always spawn you EXACTLY somewhere, this is the error to allow
		if (Math.abs(a1[0]-a2[0])<th || Math.abs(a1[1]-a2[1])<th){
			return true;
		}
		return false;
	}
	
	public void updateGlobalSpawnLocation(Player player){
		Location worldSpawnLocation = this.getServer().getWorlds().get(0).getSpawnLocation();
		Integer currentServerSpawn[] = {worldSpawnLocation.getBlockX(),worldSpawnLocation.getBlockZ()};
		Integer playerSpawn[] = spawnData.get(player.getUniqueId());
		if(spawnProximityChecker(currentServerSpawn, playerSpawn)){
			newRandomWorldSpawn(this.getServer().getWorlds().get(0)); // overworld
//			newRandomWorldSpawn(this.getServer().getWorlds().get(1)); // nether //don't really need to do this
			Integer spawnsGenerated = this.getConfig().getInt("totalSpawnsGenerated");
			this.getConfig().set("totalSpawnsGenerated", spawnsGenerated + 1);
		    saveConfig();
		}
//		player.setBedSpawnLocation(worldSpawnLocation, false);
	}
	
	public void updatePlayerDeathData(Player p, Location ploc){
		Integer coords[] = {ploc.getBlockX(),ploc.getBlockZ()};
		spawnData.put(p.getUniqueId(), coords);
		saveSpawnData();
	}
	
	public void loadSpawnData(){
		FileInputStream fis;
		try {
			fis = new FileInputStream("plugins/AlmostHardcore/" + this.getConfig().getString("TrackingDataFile"));
		    ObjectInputStream ois = new ObjectInputStream(fis);
		    this.spawnData = (HashMap<UUID, Integer[]>) ois.readObject();
		    ois.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveSpawnData(){
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("plugins/AlmostHardcore/" + this.getConfig().getString("TrackingDataFile"));
		    ObjectOutputStream oos = new ObjectOutputStream(fos);
		    oos.writeObject(this.spawnData);
		    oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
