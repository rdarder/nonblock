package com.globant.nonblock.netty.server.service.geo.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.globant.nonblock.netty.server.channel.BroadcastClientChannelSet;
import com.globant.nonblock.netty.server.channel.ClientChannel;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;
import com.globant.nonblock.netty.server.service.geo.GeoNode;
import com.globant.nonblock.netty.server.service.geo.GeoTreeWalker;
import com.globant.nonblock.netty.server.service.location.LocationType;
import com.google.inject.Provider;

public class ChannelGroupGeoNode implements GeoNode {

	private final GeoNode parent;

	private final LocationType locationType;

	private final String location;

	private final Map<SubscribeMessage, BroadcastClientChannelSet> subscribers = new HashMap<SubscribeMessage, BroadcastClientChannelSet>();

	private final Provider<BroadcastClientChannelSet> broadcastChannelFactory;

	public ChannelGroupGeoNode(final GeoNode parent, final LocationType locationType, final String location, final Provider<BroadcastClientChannelSet> broadcastChannelFactory) {
		super();
		this.parent = parent;
		this.locationType = locationType;
		this.location = location;
		this.broadcastChannelFactory = broadcastChannelFactory;
	}

	public void addSubscriptor(final SubscribeMessage message, final ClientChannel channel) {

		Validate.isTrue(message.getAlcance().equals(this.locationType), "Wrong location type");
		Validate.isTrue(message.getAlcanceValue().equals(this.location), "Wronh location");

		if (subscribers.get(message) == null) {
			subscribers.put(message, broadcastChannelFactory.get());
		}

		subscribers.get(message).add(channel);
	}

	public void traverse(final GeoTreeWalker geoTreeVisitor) {
		if (geoTreeVisitor.childFirst()) {
			geoTreeVisitor.visit(this);
		}
		if (this.parent != null) {
			this.parent.traverse(geoTreeVisitor);
		}
		if (!geoTreeVisitor.childFirst()) {
			geoTreeVisitor.visit(this);
		}
	}

	public Set<SubscribeMessage> getAllSubscriberMessages() {
		return this.subscribers.keySet();
	}

	public BroadcastClientChannelSet getChannelGroup(final SubscribeMessage message) {
		return this.subscribers.get(message);
	}

}
