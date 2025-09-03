package com.beny.libgdx3d1;

import java.util.UUID;
import com.badlogic.gdx.math.Vector3;


public class PojoNetworkUpdate {
	private UUID uuid;
	private String actType;
	private Vector3 rot;
	private Float rotSpeed; 
	private String ani; 
	private Vector3 pos;
	private Float heading;
	
	public UUID getUuid() {
		return this.uuid;
	}
	public String getActType() {
		return this.actType;
	}
	
	
	public Vector3 getRot() {
		return this.rot;
	}
	public Float getRotSpeed() {
		return this.rotSpeed;
	}
	public String getAni() {
		return this.ani;
	}
	public Vector3 getPos() {
		return this.pos;
	}
	public Float getHeading() {
		return this.heading;
	}

	public void setRot(Vector3 rot) {
		this.rot = rot;
	}
	public void setRotSpeed(Float rotSpeed) {
		this.rotSpeed = rotSpeed;
	}
	public void setAni(String ani) {
		this.ani = ani;
	}
	public void setPos(Vector3 pos) {
		this.pos = pos;
	}
	public void setHeading(Float heading) {
		this.heading = heading;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	public void setActType(String actType) {
		this.actType = actType;
	}

	
}