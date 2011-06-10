package com.globant.nonblock.netty.server.service.location;

public class Location {

	private String mesa;

	private String local;

	private String seccional;

	private String localidad;

	private String departamento;

	private String provincia;

	public Location() {
	}

	public Location(String mesa, String local, String seccional, String localidad, String departamento, String provincia) {
		super();
		this.mesa = mesa;
		this.local = local;
		this.seccional = seccional;
		this.localidad = localidad;
		this.departamento = departamento;
		this.provincia = provincia;
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

	public String getValueByType(LocationType type) {
		switch (type) {
		case Departamento:
			return this.departamento;
		case Local:
			return this.local;
		case Localidad:
			return this.localidad;
		case Mesa:
			return this.mesa;
		case Provincia:
			return this.provincia;
		case Seccional:
			return this.seccional;
		}
		throw new IllegalArgumentException();
	}

}
