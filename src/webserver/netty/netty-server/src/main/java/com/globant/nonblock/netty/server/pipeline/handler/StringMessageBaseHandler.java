package com.globant.nonblock.netty.server.pipeline.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public abstract class StringMessageBaseHandler extends SimpleChannelUpstreamHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		final String message = (String) e.getMessage();
		processMessage(message, ctx.getChannel());
	}

	abstract void processMessage(String message, Channel channel);

}
