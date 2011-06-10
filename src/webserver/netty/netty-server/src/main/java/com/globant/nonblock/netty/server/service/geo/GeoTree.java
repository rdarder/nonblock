package com.globant.nonblock.netty.server.service.geo;

import com.globant.nonblock.netty.server.lifecycle.LifecycleComponent;
import com.globant.nonblock.netty.server.service.location.LocationType;

public interface GeoTree extends LifecycleComponent {

	GeoNode findGeoNode(LocationType locationType, String locationName);

}
