package com.globant.nonblock.netty.server.service.geo.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.Validate;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.globant.nonblock.netty.server.channel.ClientChannel;
import com.globant.nonblock.netty.server.channel.impl.NettyClientChannelAdapter;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;
import com.globant.nonblock.netty.server.service.geo.GeoNode;
import com.globant.nonblock.netty.server.service.geo.GeoTreeWalker;
import com.globant.nonblock.netty.server.service.geo.SubscriptionEntry;
import com.globant.nonblock.netty.server.service.location.LocationType;

public class ChannelGroupGeoNode implements GeoNode {

	private final GeoNode parent;

	private final LocationType locationType;

	private final String location;

	private AtomicBoolean dirty = new AtomicBoolean(false);

	private final Map<SubscribeMessage, Set<SubscriptionEntry>> subscribersByNivel = new HashMap<SubscribeMessage, Set<SubscriptionEntry>>();

	public ChannelGroupGeoNode(final GeoNode parent, final LocationType locationType, final String location) {
		super();
		this.parent = parent;
		this.locationType = locationType;
		this.location = location;
	}

	public void addSubscriptor(final SubscribeMessage message, final ClientChannel channel) {

		Validate.isTrue(message.getAlcance().equals(this.locationType), "Wrong location type");
		Validate.isTrue(message.getLugar().equals(this.location), "Wrong location");

		final SubscriptionEntry entry = new SubscriptionEntry();
		entry.clientChannel = channel;
		entry.originalSubscribeMessage = message;

		
		final NettyClientChannelAdapter nettyChannel = ((NettyClientChannelAdapter) channel);
		nettyChannel.getWrappedChannel().getCloseFuture().addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				subscribersByNivel.get(message).remove(entry);				
			}
		});
		
		if (subscribersByNivel.get(message) == null) {
			subscribersByNivel.put(message, new HashSet<SubscriptionEntry>());
		}

		subscribersByNivel.get(message).add(entry);
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
		return this.subscribersByNivel.keySet();
	}

	public Set<SubscriptionEntry> getChannelGroup(final SubscribeMessage message) {
		return this.subscribersByNivel.get(message);
	}

	@Override
	public boolean isDirty() {
		return this.dirty.get();
	}

	@Override
	public void setDirty() {
		this.dirty.set(true);
	}

	@Override
	public void clean() {
		this.dirty.set(false);
	}

}
