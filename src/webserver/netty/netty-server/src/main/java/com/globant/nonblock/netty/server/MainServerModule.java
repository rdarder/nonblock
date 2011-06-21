package com.globant.nonblock.netty.server;

import com.globant.nonblock.netty.server.channel.impl.NettyChannelsAdapterModule;
import com.globant.nonblock.netty.server.config.ServerOptionsModule;
import com.globant.nonblock.netty.server.log.LogModule;
import com.globant.nonblock.netty.server.pipeline.handler.PipelineModule;
import com.globant.nonblock.netty.server.service.ServiceAggregatorModule;
import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

public class MainServerModule extends AbstractModule {

	private static final String JPA_PERSIST_UNIT_NAME = "mysqlpu";

	@Override
	protected void configure() {
		install(new JpaPersistModule(JPA_PERSIST_UNIT_NAME));
		install(new ServiceAggregatorModule());
		install(new NettyChannelsAdapterModule());
		install(new ServerOptionsModule());
		install(new LogModule());
		install(new PipelineModule());
	}

}
