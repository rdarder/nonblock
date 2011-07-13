package com.globant.nonblock.netty.server.pipeline.handler;

import javax.inject.Inject;

import org.jboss.netty.channel.Channel;

import com.globant.nonblock.netty.server.channel.ClientChannel;
import com.globant.nonblock.netty.server.channel.impl.NettyClientChannelAdapter;
import com.globant.nonblock.netty.server.log.EventLogger;
import com.globant.nonblock.netty.server.log.event.SubscribeReceivedEvent;
import com.globant.nonblock.netty.server.message.MessageParser;
import com.globant.nonblock.netty.server.message.binding.Message;
import com.globant.nonblock.netty.server.message.newdata.NewDataMessage;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;
import com.globant.nonblock.netty.server.service.geo.GeoNode;
import com.globant.nonblock.netty.server.service.geo.GeoTree;
import com.globant.nonblock.netty.server.service.mapping.NewDataMessageBuilder;
import com.globant.nonblock.netty.server.service.votes.VoteService;

public final class MessageManagerHandler extends StringMessageBaseHandler {

	private final GeoTree geoTree;

	private final VoteService voteService;
	
	private final EventLogger eventLogger;

	private final MessageParser messageParser = new MessageParser();

	@Inject
	public MessageManagerHandler(final GeoTree geoTree, final VoteService voteService, final EventLogger eventLogger) {
		super();
		this.geoTree = geoTree;
		this.voteService = voteService;
		this.eventLogger = eventLogger;
	}

	@Override
	void processMessage(final String message, final Channel c) {
		final Message cm = parseMessage(message);
		if (cm == null)
			return;
		if (cm instanceof SubscribeMessage) {
			SubscribeMessage sm = (SubscribeMessage) cm;
			this.eventLogger.process(new SubscribeReceivedEvent(sm));
			final GeoNode geoNode = this.geoTree.findGeoNode(sm.getAlcance(), sm.getLugar());
			final ClientChannel clientChannel = new NettyClientChannelAdapter(c);
			NewDataMessage msg = NewDataMessageBuilder.buildFromQuery(this.voteService.calculateStatus(sm), sm);
			clientChannel.write(msg.toJson() + "\n");
			geoNode.addSubscriptor(sm, clientChannel);
		}
	}

	private Message parseMessage(final String message) {
		try {
			final Message cm = this.messageParser.parseClientMessage(message);
			return cm;
		} catch (Exception e) {
			return null;
		}

	}

}
