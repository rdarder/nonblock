package com.globant.nonblock.netty.server.channel.impl;

import org.jboss.netty.channel.Channel;

import com.globant.nonblock.netty.server.channel.ClientChannel;

public class NettyClientChannelAdapter implements ClientChannel {

	private Channel channel;

	public NettyClientChannelAdapter(Channel channel) {
		super();
		this.channel = channel;
	}

	Channel getWrappedChannel() {
		return channel;
	}

}
