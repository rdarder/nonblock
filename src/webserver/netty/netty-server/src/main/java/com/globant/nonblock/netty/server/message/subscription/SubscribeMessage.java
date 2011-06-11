package com.globant.nonblock.netty.server.message.subscription;

import java.util.Map;

import net.sf.json.JSONObject;

import com.globant.nonblock.netty.server.message.ClientMessage;
import com.globant.nonblock.netty.server.service.location.LocationType;

public class SubscribeMessage implements ClientMessage {

	private String puesto;

	private LocationType nivel;

	private LocationType alcance;

	private String alcanceValue;

	public String getPuesto() {
		return puesto;
	}

	public void setPuesto(String puesto) {
		this.puesto = puesto;
	}

	public LocationType getNivel() {
		return nivel;
	}

	public void setNivel(LocationType nivel) {
		this.nivel = nivel;
	}

	public LocationType getAlcance() {
		return alcance;
	}

	public void setAlcance(LocationType alcance) {
		this.alcance = alcance;
	}

	public String getAlcanceValue() {
		return alcanceValue;
	}

	public void setAlcanceValue(String alcanceValue) {
		this.alcanceValue = alcanceValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alcance == null) ? 0 : alcance.hashCode());
		result = prime * result + ((alcanceValue == null) ? 0 : alcanceValue.hashCode());
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
		if (alcanceValue == null) {
			if (other.alcanceValue != null)
				return false;
		} else if (!alcanceValue.equals(other.alcanceValue))
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

	@Override
	public void fromString(final String json) {
		Map<String, Object> jo = JSONObject.fromObject(json);
		this.puesto = (String) jo.get("puesto");
		this.alcance = LocationType.valueOf(LocationType.class, (String) jo.get("alcance"));
		this.nivel = LocationType.valueOf(LocationType.class, (String) jo.get("nivel"));
		this.alcanceValue = (String) jo.get("valor_alcance");
		System.out.println(jo);
	}

}
