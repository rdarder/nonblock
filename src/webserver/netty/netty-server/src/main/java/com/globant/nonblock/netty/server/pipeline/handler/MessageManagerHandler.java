package com.globant.nonblock.netty.server.pipeline.handler;

import javax.inject.Inject;

import org.jboss.netty.channel.Channel;

import com.globant.nonblock.netty.server.channel.ClientChannel;
import com.globant.nonblock.netty.server.channel.impl.NettyClientChannelAdapter;
import com.globant.nonblock.netty.server.message.ClientMessage;
import com.globant.nonblock.netty.server.message.MessageParser;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;
import com.globant.nonblock.netty.server.service.geo.GeoNode;
import com.globant.nonblock.netty.server.service.geo.GeoTree;
import com.globant.nonblock.netty.server.service.votes.VoteService;

public class MessageManagerHandler extends StringMessageBaseHandler {

	private final GeoTree geoTree;

	private final VoteService voteService;
	
	private final MessageParser messageParser = new MessageParser();

	@Inject
	public MessageManagerHandler(final GeoTree geoTree, final VoteService voteService) {
		super();
		this.geoTree = geoTree;
		this.voteService = voteService;
	}

	@Override
	void processMessage(final String message, final Channel c) {
		final ClientMessage cm = parseMessage(message);
		if (cm == null)
			return;
		if (cm instanceof SubscribeMessage) {
			SubscribeMessage sm = (SubscribeMessage) cm;
			final GeoNode geoNode = this.geoTree.findGeoNode(sm.getAlcance(), sm.getAlcanceValue());
			final ClientChannel clientChannel = new NettyClientChannelAdapter(c);
			clientChannel.write(this.voteService.calculateStatus(sm).toJson() + "\n");
			geoNode.addSubscriptor(sm, clientChannel);
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
