package org.c0z3n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.bukkit.scheduler.BukkitScheduler;

public class AlmostHardcore extends JavaPlugin{	
	private com.avaje.ebean.EbeanServer db;
	
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
				hcp.setNightsAlive(0);
				updateGlobalSpawnLocation(player);
				player.getEnderChest().clear();
				db.save(hcp);
			}
			
			@EventHandler
			public void onJoin(PlayerJoinEvent e) {
				Player player = e.getPlayer();
				if(db.find(hardcorePlayer.class).where().eq("id", player.getUniqueId()).findRowCount() == 0){ 
					// if a player joins who isn't in our database yet, we need to create a 
					// hardcorePlayer database object for them and save it to the database
					hardcorePlayer newPlayer = new hardcorePlayer();
					newPlayer.initializeFromPlayer(player);
					db.save(newPlayer);
				}
			}
			
			@EventHandler
			public void onSpawn(PlayerRespawnEvent e) {
				// things to do when a player spawns
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
	    

		long enableTime = getServer().getWorlds().get(0).getTime();
        System.out.println("server starting at world time " + String.valueOf(enableTime) );
        long sunriseOffset = 24000L - enableTime; // how many ticks until t=0L
        
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() { // important for this to be sync, not async
        	// this is how we schedule things to happen every day at sunrise ( time = 0L )
            @Override
            public void run() {
                System.out.println("evaluating nights alive for online players");
                Collection<? extends Player> onlinePlayers = getServer().getOnlinePlayers();
                // at t = 0L, iterate through everyone online and add them a day alive.
                // this is a pretty naive way of counting this, it really just counts how
                // many sunrises a player was online for, but its still fun.
                for (Player player : onlinePlayers){
                	hardcorePlayer hcp = db.find(hardcorePlayer.class).where().eq("id", player.getUniqueId()).findUnique();
                	hcp.addNightAlive();
        			player.sendMessage(ChatColor.GREEN + "You survived another night! that makes " + ChatColor.GOLD + String.valueOf(hcp.getNightsAlive()) + ChatColor.GREEN + " in a row!");
        			player.sendMessage(ChatColor.GREEN + "Your record is " + ChatColor.GOLD + String.valueOf(hcp.getRecordNightsAlive()) + ChatColor.GREEN + " nights survived.");
    				db.save(hcp);
                }
            }
        }, sunriseOffset, 24000L);
        // this might cause a compounding error if the server is on for a long long time- because the server gets all
        // lagged up when it generates a new spawn, and it might lose ticks trying to recover from that. I'll have
        // to do some investigation. if it does in fact lose ticks, repeating this task exactly every 24000L will cause 
        // it to slowly drift away from sunrise a few ticks at a time.
	    
	    saveConfig();
	}
	
	@Override
	public void onDisable() {
		// what to do when the plugin is disabled (server shutting down, etc.)
		saveConfig();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		//commands
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
		// database boilerplate and setup
		try {
			this.getDatabase().find(hardcorePlayer.class).findRowCount();
		}
		catch (PersistenceException ex) {
            System.out.println(getDescription().getName() + " is setting up database");
            installDDL();
        }
	}
	

    @Override
    // this is basically boilerplate to make the database work right
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        // need to have a list.add(x) here for every class we want to be using in the database
        list.add(hardcorePlayer.class);
        list.add(hardcoreSpawn.class);
        return list;
    }
	
	public int randCoord() {
		//new random coordinate
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
		//generate a new random spawn location, ruling out some unplayable biomes
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
		//compare two locations to see if they are "equal" in the context of spawn locations
		// where a player can spawn "at" a spawn location but actually enter the world several blocks away
		// this needs work. and we could probably do without it. there is definitely a better way.
		Arrays.sort(a1);
		Arrays.sort(a2);
		int th = 25; //threshold - minecraft doesn't always spawn you EXACTLY somewhere, this is the deviation to allow
		if (Math.abs(a1[0]-a2[0])<th || Math.abs(a1[1]-a2[1])<th){
			System.out.println("generating new spawn");
			return true;
		}
		return false;
	}
	
	public void updateGlobalSpawnLocation(Player player){
		//determine if we need to move the world spawn location based on who died and the do it (or don't)
		Location worldSpawnLocation = this.getServer().getWorlds().get(0).getSpawnLocation();
		Integer currentServerSpawn[] = {worldSpawnLocation.getBlockX(),worldSpawnLocation.getBlockZ()};
		hardcorePlayer hcp = db.find(hardcorePlayer.class).where().eq("id", player.getUniqueId()).findUnique();
		Integer playerSpawn[] = {(int) hcp.getLastSpawnX(),(int) hcp.getLastSpawnZ()};
		if(spawnProximityChecker(currentServerSpawn, playerSpawn)){
			newRandomWorldSpawn(this.getServer().getWorlds().get(0));
		}
	}
	
}
