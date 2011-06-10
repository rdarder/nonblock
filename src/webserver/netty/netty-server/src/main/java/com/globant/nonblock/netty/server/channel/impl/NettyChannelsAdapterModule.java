package com.globant.nonblock.netty.server.channel.impl;

import com.globant.nonblock.netty.server.channel.BroadcastClientChannelSet;
import com.google.inject.AbstractModule;

public class NettyChannelsAdapterModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(BroadcastClientChannelSet.class).to(NettyBroadcastClientChannelSetAdapter.class);
	}

}
