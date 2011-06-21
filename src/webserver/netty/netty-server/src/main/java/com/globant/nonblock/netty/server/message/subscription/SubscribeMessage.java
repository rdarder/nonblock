package com.globant.nonblock.netty.server.message.subscription;

import net.sf.json.JSONObject;

import com.globant.nonblock.netty.server.message.binding.Message;
import com.globant.nonblock.netty.server.service.location.LocationType;

public class SubscribeMessage extends Message {

	public SubscribeMessage(Integer id, Integer ref) {
		super("subscribe", id, ref);
	}

	public SubscribeMessage() {
		super("subscribe");
	}
	
	private String puesto;

	private LocationType nivel;

	private LocationType alcance;

	private String lugar;

	public String getPuesto() {
		return puesto;
	}

	public LocationType getNivel() {
		return nivel;
	}

	public LocationType getAlcance() {
		return alcance;
	}

	public String getLugar() {
		return lugar;
	}

	@Override
	public void addBodyContent(final JSONObject data) {
		
	}

	@Override
	public void parseBodyContent(final JSONObject msg) {
		final JSONObject dataNode = (JSONObject) msg.get("data");
		this.puesto = (String) dataNode.get("puesto");
		this.alcance = LocationType.valueOf(LocationType.class, (String) dataNode.get("alcance"));
		this.nivel = LocationType.valueOf(LocationType.class, (String) dataNode.get("nivel"));
		this.lugar = (String) dataNode.get("lugar");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alcance == null) ? 0 : alcance.hashCode());
		result = prime * result + ((lugar == null) ? 0 : lugar.hashCode());
		result = prime * result + ((nivel == null) ? 0 : nivel.hashCode());
		result = prime * result + ((puesto == null) ? 0 : puesto.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubscribeMessage other = (SubscribeMessage) obj;
		if (alcance == null) {
			if (other.alcance != null)
				return false;
		} else if (!alcance.equals(other.alcance))
			return false;
		if (lugar == null) {
			if (other.lugar != null)
				return false;
		} else if (!lugar.equals(other.lugar))
			return false;
		if (nivel == null) {
			if (other.nivel != null)
				return false;
		} else if (!nivel.equals(other.nivel))
			return false;
		if (puesto == null) {
			if (other.puesto != null)
				return false;
		} else if (!puesto.equals(other.puesto))
			return false;
		return true;
	}

}
