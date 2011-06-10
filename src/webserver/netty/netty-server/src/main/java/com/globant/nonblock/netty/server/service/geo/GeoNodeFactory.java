package com.globant.nonblock.netty.server.service.geo;

import com.globant.nonblock.netty.server.service.location.LocationType;

public interface GeoNodeFactory {

	GeoNode create(GeoNode parent, LocationType locationType, String location);
	
}
