package com.globant.nonblock.netty.server.service;

import com.globant.nonblock.netty.server.service.geo.impl.GeoNettyChannelModule;
import com.globant.nonblock.netty.server.service.location.impl.JpaLocationModule;
import com.globant.nonblock.netty.server.service.votes.impl.JpaVoteModule;
import com.google.inject.AbstractModule;

public class ServiceAggregatorModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new GeoNettyChannelModule());
		install(new JpaLocationModule());
		install(new JpaVoteModule());
	}

}
