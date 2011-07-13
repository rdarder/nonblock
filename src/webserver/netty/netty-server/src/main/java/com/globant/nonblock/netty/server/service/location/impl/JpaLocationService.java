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

/**
 * JPA backed {@link LocationService}.
 * 
 * @author Julian Gutierrez Oschmann
 *
 */
public class JpaLocationService implements LocationService {

	/**
	 * JPA entity manager (Unit of Work).
	 */
	private final Provider<EntityManager> entityManager;
	
	/**
	 * Locations indexed by root level location name.
	 */
	private Map<String, Location> locationTree;

	/**
	 * Load locations HQL query.
	 */
	private static final String loadLocationsQuery = 
		" select new com.globant.nonblock.netty.server.service.location.Location(name,		" +
		"        parent.name,																" +
		"	  	 parent.parent.name, 														" +
		"		 parent.parent.parent.name, 												" +
		"	  	 parent.parent.parent.parent.name,											" +
		"		 parent.parent.parent.parent.parent.name)									" +
		"   from geo 																		" +
		"  where type = 'Mesa'																";

	@Inject
	public JpaLocationService(final Provider<EntityManager> entityManager) {
		this.entityManager = entityManager;
	}

	@Transactional
	public void start() {
		final List<Location> queryResult = findLocations();
		buildIndex(queryResult);
	}

	private void buildIndex(final List<Location> queryResult) {
		this.locationTree = new TreeMap<String, Location>();
		for (final Location l : queryResult) {
			this.locationTree.put(l.getMesa(), l);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Location> findLocations() {
		final Query q = this.entityManager.get().createQuery(loadLocationsQuery);
		final List<Location> queryResult = q.getResultList();
		return queryResult;
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
