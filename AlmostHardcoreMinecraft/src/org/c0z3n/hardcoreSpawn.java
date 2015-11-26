package org.c0z3n;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.Location;

import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name="ahm_spawns")
public class hardcoreSpawn {
	
	@Id
	private int id;

	@NotNull
	private double X;
	
	@NotNull
	private double Y;
	
	@NotNull
	private double Z;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getX() {
		return X;
	}

	public void setX(double x) {
		X = x;
	}

	public double getY() {
		return Y;
	}

	public void setY(double y) {
		Y = y;
	}

	public double getZ() {
		return Z;
	}

	public void setZ(double z) {
		Z = z;
	}
	
	public void initializeFromLocation(Location loc){
    	this.setX(loc.getX());
    	this.setY(loc.getY());
    	this.setZ(loc.getZ());
		
	}
	
}
