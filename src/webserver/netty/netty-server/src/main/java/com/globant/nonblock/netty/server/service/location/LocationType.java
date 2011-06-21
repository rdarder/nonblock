package com.globant.nonblock.netty.server.service.location;

public enum LocationType {

	Provincia(),
	Departamento(),
	Localidad(),
	Seccional(),
	Local(),
	Mesa();
	
	public LocationType getParent() {
		switch (this) {
		case Mesa:
			return Local;
		case Local:
			return Seccional;
		case Seccional:
			return Localidad;
		case Localidad:
			return Departamento;
		case Departamento:
			return Provincia;
		case Provincia:
			return null;
		}
		return null;
	}
}
