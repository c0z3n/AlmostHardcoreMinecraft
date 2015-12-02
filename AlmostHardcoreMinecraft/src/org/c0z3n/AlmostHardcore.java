package org.c0z3n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.persistence.PersistenceException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;


public class AlmostHardcore extends JavaPlugin{	
	private com.avaje.ebean.EbeanServer db;

	Random rnd = new Random();
	
	@Override
	public void onEnable() {

		this.loadDatabase();
		this.db = this.getDatabase();
		
		if (db.find(hardcoreSpawn.class).findRowCount() <= 0){
			// if there are no spawn locations in the spawn location database, we need to add the initial
			// server spawn to the database as the first entry
			hardcoreSpawn initialServerSpawn = new hardcoreSpawn();
			initialServerSpawn.initializeFromLocation(getServer().getWorlds().get(0).getSpawnLocation());
			db.save(initialServerSpawn);
		} 
		
		// event handlers
		this.getServer().getPluginManager().registerEvents(new Listener(){

			@EventHandler
			public void onDie(PlayerDeathEvent e) {
				Player player = e.getEntity();
				hardcorePlayer hcp = db.find(hardcorePlayer.class).where().eq("id", player.getUniqueId()).findUnique();
				hcp.addDeath();
				hcp.setNightsAlive(0);
				updateGlobalSpawnLocation(player);
				ItemStack[] endChestInv = player.getEnderChest().getContents();
				List<hardcoreEnderChest> enderChestList = db.find(hardcoreEnderChest.class).where().eq("owner", player.getUniqueId()).findList();
				hardcoreEnderChest[] enderChestArray = enderChestList.toArray(new hardcoreEnderChest[enderChestList.size()]);

            	
                for (hardcoreEnderChest endChest : enderChestArray){
                	// turn all ender chests into regular chests
                	World world = getServer().getWorld(endChest.getWorld());
                    int bX = (int)endChest.getX();
                	int bY = (int)endChest.getY();
                	int bZ = (int)endChest.getZ();
                	Block chestBlock = world.getBlockAt(bX,bY,bZ);
                	Material convertToMaterial = Material.TRAPPED_CHEST;
                	if(world.getBlockAt(bX + 1, bY, bZ).getType() != Material.CHEST && world.getBlockAt(bX - 1, bY, bZ).getType() != Material.CHEST && world.getBlockAt(bX, bY, bZ + 1).getType() != Material.CHEST && world.getBlockAt(bX, bY, bZ - 1).getType() != Material.CHEST){
                		convertToMaterial = Material.CHEST;
                	}
                	chestBlock.breakNaturally(null);
                	chestBlock.setType(convertToMaterial);
                }
                
                for (ItemStack item : endChestInv){
                	// place each item from the player's end chest into one of the new chests at random
                	if (item != null){
	                	int chosenChestIdx = rnd.nextInt(enderChestArray.length);
	                	hardcoreEnderChest endChest = enderChestArray[chosenChestIdx];
	                	World world = getServer().getWorld(endChest.getWorld());
	                	Chest chest = (Chest) world.getBlockAt((int)endChest.getX(),(int)endChest.getY(),(int)endChest.getZ()).getState();
	                	Inventory chestInv = chest.getInventory();
	                	chestInv.addItem(item);
                	}
                }
                
                for (hardcoreEnderChest endChest : enderChestList){
                	// delete the old end chests from the database
                	db.delete(endChest);
                }
                
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
					newPlayer.setLastSpawnId(db.find(hardcoreSpawn.class).findRowCount());
					db.save(newPlayer);
				}
			}

			@EventHandler
			public void onSleepAttempt(PlayerBedEnterEvent e) {
				// beds ruin both the spawning mechanic we want to create and
				// also the timekeeping/counting nights code, so i'm completely
				// neutering them. maybe someday I will slightly de-neuter them.
				// I doubt it.
				e.setCancelled(true);
			}

			@EventHandler
			public void onSpawn(PlayerRespawnEvent e) {
				// things to do when a player spawns
				Player player = e.getPlayer();
				hardcorePlayer hcp = db.find(hardcorePlayer.class).where().eq("id", player.getUniqueId()).findUnique();
				hcp.setLastSpawnId(db.find(hardcoreSpawn.class).findRowCount());
				db.save(hcp);
			}
			
			@EventHandler
			public void onBlockPlace(BlockPlaceEvent e) {
				if ( e.getBlockPlaced().getType() == Material.ENDER_CHEST){
					hardcoreEnderChest newEndChest = new hardcoreEnderChest();
					newEndChest.initFromPlaceEvent(e);
					db.save(newEndChest);
				}
			}
			
			@EventHandler
			public void onBlockBreak(BlockBreakEvent e) {
				if ( e.getBlock().getType() == Material.ENDER_CHEST && db.find(hardcoreEnderChest.class).where().eq("x", e.getBlock().getX()).eq("y", e.getBlock().getY()).eq("z", e.getBlock().getZ()).findRowCount() > 0){
					hardcoreEnderChest endChest = db.find(hardcoreEnderChest.class).where().eq("x", e.getBlock().getX()).eq("y", e.getBlock().getY()).eq("z", e.getBlock().getZ()).findUnique();
					db.delete(endChest);
				}
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
        // to do some investigation. if it does in fact lose ticks, repeating this task exactly every 24000L ticks will 
        // cause it to slowly drift away from sunrise a few ticks at a time, since a minecraft day might not really be 
        // exactly 24000L ticks anymore
	    
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
		
		if (cmd.getName().equalsIgnoreCase("mydeaths") && sender instanceof Player){
			hardcorePlayer hcp = db.find(hardcorePlayer.class).where().eq("id", player.getUniqueId()).findUnique();
			player.sendMessage(ChatColor.RED + "you have survived for " + ChatColor.GOLD + hcp.getNightsAlive() + ChatColor.RED + " nights!");
			player.sendMessage(ChatColor.RED + "your personal record is " + ChatColor.GOLD + hcp.getRecordNightsAlive() + ChatColor.RED + " nights.");
			player.sendMessage(ChatColor.RED + "you have died " + ChatColor.GOLD + hcp.getDeaths() + ChatColor.RED + " times.");
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("deaths") && sender instanceof Player){
			List<hardcorePlayer> allPlayersFromDb = db.find(hardcorePlayer.class).findList();
			getServer().broadcastMessage(ChatColor.DARK_RED + "Player   |  total  |   nights   | most nights ");
			getServer().broadcastMessage(ChatColor.DARK_RED + " name    | deaths | survived |   survived  ");
			getServer().broadcastMessage(ChatColor.DARK_RED + "--------------------------------------------");
			for (hardcorePlayer hcp : allPlayersFromDb){
				Integer spaces = 16 - hcp.getPlayerName().length();
				String whitespace = "";
				for(int i = 0; i< spaces; i++){
					whitespace = whitespace + " ";
				}
				getServer().broadcastMessage(ChatColor.RED + hcp.getPlayerName() + whitespace + "|   " + hcp.getDeaths() + "   |   " + hcp.getNightsAlive() + "   |   " + hcp.getRecordNightsAlive());
			}
			return true;
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
        // need to have a list.add() here for every class we want to be using in the database
        list.add(hardcorePlayer.class);
        list.add(hardcoreSpawn.class);
        list.add(hardcoreEnderChest.class);
        return list;
    }
	
	private int randCoord() {
		//new random coordinate
		int randWindowSize = this.getConfig().getInt("RandomSpawnWindowSize");
		return rnd.nextInt(randWindowSize) - randWindowSize/2;	
	}
	
	private Block newRandomSurfaceBlock(World w) {	
		// picks a new random block at the surface of the world
		Location randLocation = new Location(w, randCoord(), 0, randCoord());
		return w.getHighestBlockAt(randLocation);
	}

	private void newRandomWorldSpawn(World w) {
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
	
	private void updateGlobalSpawnLocation(Player player){
		hardcorePlayer hcp = db.find(hardcorePlayer.class).where().eq("id", player.getUniqueId()).findUnique();
		if(hcp.getLastSpawnId() == db.find(hardcoreSpawn.class).findRowCount()){
			newRandomWorldSpawn(this.getServer().getWorlds().get(0));
		}
	}
	
}
