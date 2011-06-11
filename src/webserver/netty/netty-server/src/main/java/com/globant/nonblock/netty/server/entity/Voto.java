package com.globant.nonblock.netty.server.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Voto {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String mesa;

	private String local;

	private String seccional;

	private String localidad;

	private String departamento;

	private String provincia;

	private String puesto;

	private String partido;

	private String candidato;

	private Long votos;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMesa() {
		return mesa;
	}

	public void setMesa(String mesa) {
		this.mesa = mesa;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public String getSeccional() {
		return seccional;
	}

	public void setSeccional(String seccional) {
		this.seccional = seccional;
	}

	public String getLocalidad() {
		return localidad;
	}

	public void setLocalidad(String localidad) {
		this.localidad = localidad;
	}

	public String getDepartamento() {
		return departamento;
	}

	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}

	public String getProvincia() {
		return provincia;
	}

	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}

	public String getPuesto() {
		return puesto;
	}

	public void setPuesto(String puesto) {
		this.puesto = puesto;
	}

	public String getPartido() {
		return partido;
	}

	public void setPartido(String partido) {
		this.partido = partido;
	}

	public String getCandidato() {
		return candidato;
	}

	public void setCandidato(String candidato) {
		this.candidato = candidato;
	}

	public Long getVotos() {
		return votos;
	}

	public void setVotos(Long votos) {
		this.votos = votos;
	}

}
