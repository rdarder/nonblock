package com.globant.nonblock.netty.server.message.newdata;

public class NewDataDatum {

	private Long cant;

	private String nivel;

	private String candidato;

	public NewDataDatum(Long cant, String nivel, String candidato) {
		super();
		this.cant = cant;
		this.nivel = nivel;
		this.candidato = candidato;
	}

	public Long getCant() {
		return cant;
	}

	public void setCant(Long cant) {
		this.cant = cant;
	}

	public String getNivel() {
		return nivel;
	}

	public void setNivel(String nivel) {
		this.nivel = nivel;
	}

	public String getCandidato() {
		return candidato;
	}

	public void setCandidato(String candidato) {
		this.candidato = candidato;
	}

}
