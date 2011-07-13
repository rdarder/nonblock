package com.globant.nonblock.netty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.globant.nonblock.netty.server.config.BootstrapParameters;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ServerBootstrap {

	private static final Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);
	
	public static void main(final String[] cmdLine) {
		BootstrapParameters.setArgs(cmdLine);

		final Injector mainInjector = Guice.createInjector(new MainServerModule());

		final Server mainServer = mainInjector.getInstance(Server.class);

		logger.info("Starting main server...");

		mainServer.start();

	}
}
