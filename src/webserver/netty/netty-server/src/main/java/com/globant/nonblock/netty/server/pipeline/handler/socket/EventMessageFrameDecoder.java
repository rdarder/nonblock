package com.globant.nonblock.netty.server.pipeline.handler.socket;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class EventMessageFrameDecoder extends FrameDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		int lenght = buffer.readableBytes();
		byte[] messageBuffer = new byte[lenght];
		buffer.readBytes(messageBuffer);
		String message = new String(messageBuffer);
		return message;
	}

}
