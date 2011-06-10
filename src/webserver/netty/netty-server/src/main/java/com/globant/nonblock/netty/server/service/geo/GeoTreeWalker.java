package com.globant.nonblock.netty.server.service.geo;

import com.globant.nonblock.netty.server.service.geo.impl.ChannelGroupGeoNode;

public interface GeoTreeWalker {

	void visit(ChannelGroupGeoNode geoNode);

	boolean childFirst();
}
