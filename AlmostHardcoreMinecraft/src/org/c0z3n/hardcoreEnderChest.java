package org.c0z3n;





import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.event.block.BlockPlaceEvent;

import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name="ahm_enderchests")
public class hardcoreEnderChest {
	
	@Id
	private int id;
	
	@NotNull
	private UUID owner;
	
	@NotNull
	private double x;
	
	@NotNull
	private double y;
	
	@NotNull
	private double z;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public void coordsFromLocation(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void initFromPlaceEvent(BlockPlaceEvent e){
		this.owner = e.getPlayer().getUniqueId();
		this.x = e.getBlock().getX();
		this.y = e.getBlock().getY();
		this.z = e.getBlock().getZ();
	}

}
