package com.globant.nonblock.netty.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name="geo")
public class Geo {

	@Id
	private Long id;

	@Column(name="nombre")
	private String name;

	@Column(name="tipo")
	private String type;

	@ManyToOne()
	@JoinColumn(name = "contenedor_id")
	private Geo parent;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Geo getParent() {
		return parent;
	}
	
	public void setParent(Geo parent) {
		this.parent = parent;
	}
	
}
