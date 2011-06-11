package com.globant.nonblock.netty.server.service.location.impl;

import com.globant.nonblock.netty.server.service.location.LocationService;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class JpaLocationModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(LocationService.class).to(JpaLocationService.class).in(Singleton.class);
	}

}
