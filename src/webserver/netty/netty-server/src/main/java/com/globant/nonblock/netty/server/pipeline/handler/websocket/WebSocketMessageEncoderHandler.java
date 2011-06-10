package com.globant.nonblock.netty.server.pipeline.handler.websocket;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class WebSocketMessageEncoderHandler extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		if (msg instanceof String) {
			return new DefaultWebSocketFrame((String) msg);
		}
		return msg;
	}

}
