package com.globant.nonblock.netty.server.service.geo;


public interface GeoTreeWalker {

	void visit(GeoNode geoNode);

	boolean childFirst();
}
