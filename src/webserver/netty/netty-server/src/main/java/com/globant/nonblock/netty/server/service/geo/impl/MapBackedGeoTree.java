package com.globant.nonblock.netty.server.service.geo.impl;

import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import com.globant.nonblock.netty.server.service.geo.GeoNode;
import com.globant.nonblock.netty.server.service.geo.GeoNodeFactory;
import com.globant.nonblock.netty.server.service.geo.GeoTree;
import com.globant.nonblock.netty.server.service.location.Location;
import com.globant.nonblock.netty.server.service.location.LocationService;
import com.globant.nonblock.netty.server.service.location.LocationType;

public class MapBackedGeoTree implements GeoTree {

	private final LocationService locationService;

	private final GeoNodeFactory geoNodeFactory;

	private final Map<LocationType, Map<String, GeoNode>> leafsNodesMap = new TreeMap<LocationType, Map<String, GeoNode>>();

	@Inject
	public MapBackedGeoTree(final LocationService locationService, final GeoNodeFactory geoNodeFactory) {
		super();
		this.locationService = locationService;
		this.geoNodeFactory = geoNodeFactory;
	}

	public GeoNode findGeoNode(final LocationType locationType, final String locationName) {
		return this.leafsNodesMap.get(locationType).get(locationName);
	}

	public void start() {
		createInitialMap();
		for (final String mesa : locationService.getAllMesas()) {
			final Location l = locationService.findLocation(mesa);
			resolveGeoNodeRecursively(l, LocationType.Mesa);
		}
	}

	private GeoNode resolveGeoNodeRecursively(final Location location, final LocationType type) {
		if (this.leafsNodesMap.get(type).get(location.getValueByType(type)) == null) {
			final LocationType parentType = type.getParent();
			if (parentType != null) {
				final GeoNode parentNode = resolveGeoNodeRecursively(location, parentType);
				final GeoNode gn = this.geoNodeFactory.create(parentNode, type, location.getValueByType(type)); 
				this.leafsNodesMap.get(type).put(location.getValueByType(type), gn);
			} else {
				final GeoNode gn = this.geoNodeFactory.create(null, type, location.getValueByType(type));
				this.leafsNodesMap.get(type).put(location.getValueByType(type), gn);
			}
		}
		return this.leafsNodesMap.get(type).get(location.getValueByType(type));
	}

	private void createInitialMap() {
		leafsNodesMap.put(LocationType.Mesa, new TreeMap<String, GeoNode>());
		leafsNodesMap.put(LocationType.Local, new TreeMap<String, GeoNode>());
		leafsNodesMap.put(LocationType.Seccional, new TreeMap<String, GeoNode>());
		leafsNodesMap.put(LocationType.Localidad, new TreeMap<String, GeoNode>());
		leafsNodesMap.put(LocationType.Departamento, new TreeMap<String, GeoNode>());
		leafsNodesMap.put(LocationType.Provincia, new TreeMap<String, GeoNode>());
	}

	@Override
	public void shutshown() {
	}

}
