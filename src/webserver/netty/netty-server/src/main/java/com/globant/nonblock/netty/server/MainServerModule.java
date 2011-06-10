package com.globant.nonblock.netty.server;

import com.globant.nonblock.netty.server.channel.impl.NettyChannelsAdapterModule;
import com.globant.nonblock.netty.server.service.ServiceAggregatorModule;
import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

public class MainServerModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new JpaPersistModule("mainpu"));
		install(new ServiceAggregatorModule());
		install(new NettyChannelsAdapterModule());
	}

}
