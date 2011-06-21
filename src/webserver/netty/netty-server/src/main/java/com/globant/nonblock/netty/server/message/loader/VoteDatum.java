package com.globant.nonblock.netty.server.message.loader;

import net.sf.json.JSONObject;

import com.globant.nonblock.netty.server.message.binding.MessagePart;


public class VoteDatum implements MessagePart {

	private String puesto;
	private String candidato;
	private String partido;
	private String cantidad;

	public String getPuesto() {
		return puesto;
	}

	public String getCandidato() {
		return candidato;
	}

	public String getPartido() {
		return partido;
	}

	public void setPuesto(String puesto) {
		this.puesto = puesto;
	}

	public void setCandidato(String candidato) {
		this.candidato = candidato;
	}

	public void setPartido(String partido) {
		this.partido = partido;
	}

	public void setCantidad(String cantidad) {
		this.cantidad = cantidad;
	}

	public String getCantidad() {
		return cantidad;
	}

	@Override
	public void addBodyContent(final JSONObject jo) {
		this.candidato = (String) jo.get("candidato");
		this.cantidad = jo.get("cant").toString();
		this.partido = (String) jo.get("partido");
		this.puesto = (String) jo.get("puesto");	
	}

	@Override
	public void parseBodyContent(final JSONObject jo) {
		this.candidato = (String) jo.get("candidato");
		this.cantidad = jo.get("cant").toString();
		this.partido = (String) jo.get("partido");
		this.puesto = (String) jo.get("puesto");		
	}

}
