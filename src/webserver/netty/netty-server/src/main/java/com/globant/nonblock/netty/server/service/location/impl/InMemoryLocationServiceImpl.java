package com.globant.nonblock.netty.server.service.location.impl;

import java.util.HashSet;
import java.util.Set;

import com.globant.nonblock.netty.server.service.location.Location;
import com.globant.nonblock.netty.server.service.location.LocationService;

class InMemoryLocationServiceImpl implements LocationService {

	@Override
	public Location findLocation(String mesa) {
		return new Location(mesa, "Escuela N317", "Seccional 1","San Gregorio","General Lopez", "Santa Fe");
	}

	@Override
	public Set<String> getAllMesas() {
		Set<String> s = new HashSet<String>();
		s.add("1");
		return s;
	}

	@Override
	public void shutshown() {
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

}
