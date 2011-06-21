package com.globant.nonblock.netty.server.config;

import java.util.Properties;

import com.globant.nonblock.netty.server.pipeline.handler.config.URLHandlerOptions;
import com.globant.nonblock.netty.server.pipeline.handler.resource.config.StaticContentOptions;
import com.globant.nonblock.netty.server.service.worker.conf.WorkerOptions;
import com.google.inject.AbstractModule;
import com.google.sitebricks.options.OptionsModule;

/**
 * Guice aggregator module for config specific components.
 * 
 * @author Julian Gutierrez Oschmann
 *
 */
public class ServerOptionsModule extends AbstractModule {

	@Override
	protected void configure() {
		
		final Properties args = BootstrapParameters.getProperties();
		install(new OptionsModule(args).options(HttpServerOptions.class));
		install(new OptionsModule(args).options(StaticContentOptions.class));
		install(new OptionsModule(args).options(URLHandlerOptions.class));
		install(new OptionsModule(args).options(WorkerOptions.class));
	}

}
