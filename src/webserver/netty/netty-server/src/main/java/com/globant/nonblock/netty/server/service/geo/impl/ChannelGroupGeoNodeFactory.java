package com.globant.nonblock.netty.server.service.geo.impl;

import javax.inject.Inject;

import com.globant.nonblock.netty.server.channel.BroadcastClientChannelSet;
import com.globant.nonblock.netty.server.service.geo.GeoNode;
import com.globant.nonblock.netty.server.service.geo.GeoNodeFactory;
import com.globant.nonblock.netty.server.service.location.LocationType;
import com.google.inject.Provider;

public class ChannelGroupGeoNodeFactory implements GeoNodeFactory {

	private final Provider<BroadcastClientChannelSet> broadcastChannelfactory;

	@Inject
	public ChannelGroupGeoNodeFactory(final Provider<BroadcastClientChannelSet> broadcastChannelfactory) {
		super();
		this.broadcastChannelfactory = broadcastChannelfactory;
	}

	@Override
	public GeoNode create(GeoNode parent, LocationType locationType, String location) {
		ChannelGroupGeoNode newNode = new ChannelGroupGeoNode(parent, locationType, location, broadcastChannelfactory);
		return newNode;
	}

}
