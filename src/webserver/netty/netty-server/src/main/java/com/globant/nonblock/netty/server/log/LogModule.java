package com.globant.nonblock.netty.server.log;

import com.globant.nonblock.netty.server.log.impl.Log4JEventLogger;
import com.google.inject.AbstractModule;

public class LogModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(EventLogger.class).to(Log4JEventLogger.class).asEagerSingleton();
	}

}
