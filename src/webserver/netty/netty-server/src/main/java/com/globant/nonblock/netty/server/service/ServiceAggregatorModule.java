package com.globant.nonblock.netty.server.service;

import com.globant.nonblock.netty.server.service.geo.impl.GeoTreeModule;
import com.globant.nonblock.netty.server.service.location.impl.JpaLocationModule;
import com.globant.nonblock.netty.server.service.votes.impl.JpaVoteModule;
import com.globant.nonblock.netty.server.service.worker.WorkersModule;
import com.google.inject.AbstractModule;

/**
 * Guice aggregator module for core services.
 * 
 * @author Julian Gutierrez Oschmann
 *
 */
public class ServiceAggregatorModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new GeoTreeModule());
		install(new JpaLocationModule());
		install(new JpaVoteModule());
		install(new WorkersModule());
	}

}
