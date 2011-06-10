package com.globant.nonblock.netty.server.pipeline.handler.socket;

import javax.inject.Inject;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.globant.nonblock.netty.server.message.MessageParser;
import com.globant.nonblock.netty.server.service.geo.GeoTree;

public class MessageDecoderHandler extends SimpleChannelUpstreamHandler {

	private final GeoTree geoTree;

	private final MessageParser messageParser = new MessageParser();

	@Inject
	public MessageDecoderHandler(final GeoTree geoTree) {
		super();
		this.geoTree = geoTree;
	}

	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		if (e.getMessage() instanceof String) {
			final String jsonMessage = (String) e.getMessage();
			messageParser.parseClientMessage(jsonMessage);
			
		}
		
		super.messageReceived(ctx, e);
	}
	
}
