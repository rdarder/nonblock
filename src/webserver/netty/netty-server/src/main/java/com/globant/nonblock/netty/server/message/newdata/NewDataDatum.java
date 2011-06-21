package com.globant.nonblock.netty.server.message.newdata;

public class NewDataDatum {

	private String puesto;
	private String mesa;
	private String local;
	private String seccional;
	private String localidad;
	private String departamento;
	private String provincia;
	private String candidato;
	private String partido;
	private Integer votos;

	public String getPuesto() {
		return puesto;
	}

	public String getMesa() {
		return mesa;
	}

	public String getLocal() {
		return local;
	}

	public String getSeccional() {
		return seccional;
	}

	public String getLocalidad() {
		return localidad;
	}

	public String getDepartamento() {
		return departamento;
	}

	public String getProvincia() {
		return provincia;
	}

	public String getCandidato() {
		return candidato;
	}

	public String getPartido() {
		return partido;
	}

	public Integer getVotos() {
		return votos;
	}

	public NewDataDatum(String puesto, String mesa, String local, String seccional, String localidad, String departamento, String provincia, String candidato, String partido,
			Integer votos) {
		super();
		this.puesto = puesto;
		this.mesa = mesa;
		this.local = local;
		this.seccional = seccional;
		this.localidad = localidad;
		this.departamento = departamento;
		this.provincia = provincia;
		this.candidato = candidato;
		this.partido = partido;
		this.votos = votos;
	}

}
