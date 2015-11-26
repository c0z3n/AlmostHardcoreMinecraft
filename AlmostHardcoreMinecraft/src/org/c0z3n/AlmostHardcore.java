package org.c0z3n;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
	com.avaje.ebean.EbeanServer db;
	
	@Override
	public void onEnable() {

		
		this.loadDatabase();
		this.db = this.getDatabase();
		
		// some event handlers
		this.getServer().getPluginManager().registerEvents(new Listener(){

			@EventHandler
			public void onDie(PlayerDeathEvent e) {
				Player player = e.getEntity();
				hardcorePlayer hcp = db.find(hardcorePlayer.class).where().eq("id", player.getUniqueId()).findUnique();
				hcp.addDeath();
				updateGlobalSpawnLocation(player);
				player.getEnderChest().clear();
				db.save(hcp);
			}
			
			@EventHandler
			public void onJoin(PlayerJoinEvent e) {
				Player player = e.getPlayer();
				if(db.find(hardcorePlayer.class).where().eq("id", player.getUniqueId()).findRowCount() == 0){
					hardcorePlayer newPlayer = new hardcorePlayer();
					newPlayer.initializeFromPlayer(player);
					db.save(newPlayer);
				}
			}
			
			@EventHandler
			public void onSpawn(PlayerRespawnEvent e) {
				Player player = e.getPlayer();
				hardcorePlayer hcp = db.find(hardcorePlayer.class).where().eq("id", player.getUniqueId()).findUnique();
				hcp.updateLastSpawn(e.getRespawnLocation());
				db.save(hcp);
			}
        
		}, this);

	    this.getConfig().addDefault("RandomSpawnWindowSize", 500000);
	    this.getConfig().addDefault("totalSpawnsGenerated", 0);
	    this.getConfig().options().copyDefaults(true);
	    this.getServer().setSpawnRadius(0);
	    saveConfig();
	}
	
	@Override
	public void onDisable() {
		saveConfig();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		Player player = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("deaths") && sender instanceof Player){
			player.sendMessage(ChatColor.RED + "command not implemented");
		}

		if (cmd.getName().equalsIgnoreCase("deathboard") && sender instanceof Player){
			player.sendMessage(ChatColor.RED + "command not implemented");
		}
		
		return false;
	}
	
	private void loadDatabase() {
		try {
			new FileOutputStream("ebean.properties", true).close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			this.getDatabase().find(hardcorePlayer.class).findRowCount();
		}
		catch (PersistenceException ex) {
            System.out.println(getDescription().getName() + " is setting up database");
            installDDL();
        }
	}
	

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(hardcorePlayer.class);
        list.add(hardcoreSpawn.class);
        return list;
    }
	
	public int randCoord() {
		Random rnd = new Random();
		int randWindowSize = this.getConfig().getInt("RandomSpawnWindowSize");
		return rnd.nextInt(randWindowSize) - randWindowSize/2;	
	}
	
	public Block newRandomSurfaceBlock(World w) {	
		// picks a new random block at the surface of the world
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
		hardcoreSpawn newSpawnDatabaseEntry = new hardcoreSpawn();
		newSpawnDatabaseEntry.initializeFromLocation(newSpawn);
		db.save(newSpawnDatabaseEntry);
	}
	
	public boolean spawnProximityChecker(Integer a1[], Integer a2[]){
		Arrays.sort(a1);
		Arrays.sort(a2);
		Integer th = 25; //threshold - minecraft doesn't always spawn you EXACTLY somewhere, this is the error to allow
		if (Math.abs(a1[0]-a2[0])<th || Math.abs(a1[1]-a2[1])<th){
			System.out.println("generating new spawn");
			return true;
		}
		return false;
	}
	
	public void updateGlobalSpawnLocation(Player player){
		Location worldSpawnLocation = this.getServer().getWorlds().get(0).getSpawnLocation();
		Integer currentServerSpawn[] = {worldSpawnLocation.getBlockX(),worldSpawnLocation.getBlockZ()};
		hardcorePlayer hcp = db.find(hardcorePlayer.class).where().eq("id", player.getUniqueId()).findUnique();
		Integer playerSpawn[] = {(int) hcp.getLastSpawnX(),(int) hcp.getLastSpawnZ()};
		if(spawnProximityChecker(currentServerSpawn, playerSpawn)){
			newRandomWorldSpawn(this.getServer().getWorlds().get(0));
		}
	}
	
}
