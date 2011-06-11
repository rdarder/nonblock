package com.globant.nonblock.netty.server.channel.impl;

import org.jboss.netty.channel.Channel;

import com.globant.nonblock.netty.server.channel.ClientChannel;

public final class NettyClientChannelAdapter implements ClientChannel {

	private final Channel channel;

	public NettyClientChannelAdapter(final Channel channel) {
		super();
		this.channel = channel;
	}

	Channel getWrappedChannel() {
		return channel;
	}

	@Override
	public void write(final String message) {
		this.channel.write(message);
	}

}
