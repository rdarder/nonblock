package com.globant.nonblock.netty.server.service.geo.impl;

import com.globant.nonblock.netty.server.service.geo.GeoNodeFactory;
import com.globant.nonblock.netty.server.service.geo.GeoTree;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class GeoNettyChannelModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(GeoTree.class).to(MapBackedGeoTree.class).in(Singleton.class);
		bind(GeoNodeFactory.class).to(ChannelGroupGeoNodeFactory.class);
	}

}
