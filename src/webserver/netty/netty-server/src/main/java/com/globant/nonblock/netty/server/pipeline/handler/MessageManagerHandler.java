package com.globant.nonblock.netty.server.pipeline.handler;

import javax.inject.Inject;

import org.jboss.netty.channel.Channel;

import com.globant.nonblock.netty.server.channel.impl.NettyClientChannelAdapter;
import com.globant.nonblock.netty.server.message.ClientMessage;
import com.globant.nonblock.netty.server.message.MessageParser;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;
import com.globant.nonblock.netty.server.service.geo.GeoNode;
import com.globant.nonblock.netty.server.service.geo.GeoTree;

public class MessageManagerHandler extends StringMessageBaseHandler {

	private final GeoTree geoTree;

	private final MessageParser messageParser = new MessageParser();

	@Inject
	public MessageManagerHandler(final GeoTree geoTree) {
		super();
		this.geoTree = geoTree;
	}

	@Override
	void processMessage(final String message, final Channel c) {
		final ClientMessage cm = parseMessage(message);
		if (cm == null)
			return;
		if (cm instanceof SubscribeMessage) {
			SubscribeMessage sm = (SubscribeMessage) cm;
			final GeoNode geoNode = this.geoTree.findGeoNode(sm.getAlcance(), sm.getAlcanceValue());
			geoNode.addSubscriptor(sm, new NettyClientChannelAdapter(c));
		}
	}

	private ClientMessage parseMessage(final String message) {
		try {
			final ClientMessage cm = this.messageParser.parseClientMessage(message);
			return cm;
		} catch (Exception e) {
			return null;
		}

	}

}
