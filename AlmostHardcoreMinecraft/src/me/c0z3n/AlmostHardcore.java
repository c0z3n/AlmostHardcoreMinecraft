package me.c0z3n;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AlmostHardcore extends JavaPlugin{
	
	
	HashMap<String, Integer[]> spawnData = new HashMap<String, Integer[]>();
	@Override
	public void onEnable() {
		
		// some event handlers
		this.getServer().getPluginManager().registerEvents(new Listener(){

			@EventHandler
			public void onDie(PlayerDeathEvent e) {
				// triggered when any player dies
				Player player = e.getEntity();
				updateGlobalSpawnLocation(player);
			}

			@EventHandler
			public void onJoin(PlayerJoinEvent e) {
				// triggered when any player joins
				Player player = e.getPlayer();
				if((spawnData.get(player.getName()) == null) || (!player.hasPlayedBefore())){
					updatePlayerSpawnData(player, getServer().getWorlds().get(0).getSpawnLocation());
				}
			}
			@EventHandler
			public void onSpawn(PlayerRespawnEvent e) {
				Player player = e.getPlayer();
				updatePlayerSpawnData(player, e.getRespawnLocation());
			}
			
		}, this);
		
	    this.getConfig().addDefault("RandomSpawnWindowSize", 500000);
	    this.getConfig().addDefault("TrackingDataFile", "trackingdata.dat");
	    this.getConfig().options().copyDefaults(true);
	    this.getServer().setSpawnRadius(0);
	    loadSpawnData();
	    saveConfig();
//	    getLogger().info(this.getServer().getWorld("world");
	}
	
	@Override
	public void onDisable() {
		saveSpawnData();
		saveConfig();
//		data.writeTrackingData(this.getConfig().getString("TrackingDataFile"));
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
		Integer th = 25;
		
		if( (a1[0]-th <= a2[0] && a2[0] <= a1[0]+th) && (a1[1]-th) <= a2[1] && a2[1] <= (a1[1]+th) ){
			return true;
		}
		if( a2[0]-th <= a1[0] && a1[0] <= a2[0]+th && a2[1]-th <= a1[1] && a1[1] <= a2[1]+th ){
			return true;
		}
		return false;
	}
	
	public void updateGlobalSpawnLocation(Player player){
		Location worldSpawnLocation = this.getServer().getWorlds().get(0).getSpawnLocation();
		Integer currentServerSpawn[] = {worldSpawnLocation.getBlockX(),worldSpawnLocation.getBlockZ()};
		Integer playerSpawn[] = spawnData.get(player.getName());
		
		
		
		if(spawnProximityChecker(currentServerSpawn, playerSpawn)){
			newRandomWorldSpawn(this.getServer().getWorlds().get(0)); // overworld
			newRandomWorldSpawn(this.getServer().getWorlds().get(1)); // nether
		}
		
//		player.setBedSpawnLocation(worldSpawnLocation, false);
	}
	
	public void updatePlayerSpawnData(Player p, Location ploc){
//		Location ploc = p.getLocation();
		Integer coords[] = {ploc.getBlockX(),ploc.getBlockZ()};
		spawnData.put(p.getName(), coords);
		saveSpawnData();
	}
	
	public void loadSpawnData(){
		FileInputStream fis;
		try {
			fis = new FileInputStream("plugins/AlmostHardcore/" + this.getConfig().getString("TrackingDataFile"));
		    ObjectInputStream ois = new ObjectInputStream(fis);
		    this.spawnData = (HashMap<String, Integer[]>) ois.readObject();
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
