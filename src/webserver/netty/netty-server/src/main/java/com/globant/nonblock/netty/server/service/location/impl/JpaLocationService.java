package com.globant.nonblock.netty.server.service.location.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.globant.nonblock.netty.server.service.location.Location;
import com.globant.nonblock.netty.server.service.location.LocationService;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class JpaLocationService implements LocationService {

	private final Provider<EntityManager> entityManager;
	private Map<String, Location> locationTree;

	@Inject
	public JpaLocationService(final Provider<EntityManager> entityManager) {
		this.entityManager = entityManager;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public void start() {
		this.locationTree = new TreeMap<String, Location>();
		final Query q = this.entityManager.get().createQuery(
				"select new com.globant.nonblock.netty.server.service.location.Location(g.name, g.parent.name, g.parent.parent.name, g.parent.parent.parent.name, "
						+ "g.parent.parent.parent.parent.name, g.parent.parent.parent.parent.parent.name) from" + " Geo g where g.type = 'Mesa'");

		List<Location> queryResult = q.getResultList();

		// Indexing by "mesa"
		for (Location l : queryResult) {
			this.locationTree.put(l.getMesa(), l);
		}
	}

	@Override
	public Location findLocation(final String mesa) {
		return this.locationTree.get(mesa);
	}

	@Override
	public Set<String> getAllMesas() {
		return this.locationTree.keySet();
	}

	@Override
	public void shutshown() {
		
	}

}
