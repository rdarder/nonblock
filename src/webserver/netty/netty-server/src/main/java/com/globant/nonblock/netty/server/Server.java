package com.globant.nonblock.netty.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.globant.nonblock.netty.server.config.HttpServerOptions;
import com.globant.nonblock.netty.server.pipeline.DefaultChannelPipelineFactory;
import com.globant.nonblock.netty.server.pipeline.RawSocketChannelPipelineFactory;
import com.globant.nonblock.netty.server.service.geo.GeoTree;
import com.globant.nonblock.netty.server.service.location.LocationService;
import com.google.inject.persist.PersistService;

public class Server {

	private final PersistService persistService;
	private final LocationService locationService;
	private final GeoTree geoTree;
	private final HttpServerOptions httpOptions;
	private final DefaultChannelPipelineFactory httpPipelineFactory;
	private final RawSocketChannelPipelineFactory rawSocketpipelineFactory;

	private static final Logger logger = LoggerFactory.getLogger(Server.class);

	@Inject
	public Server(PersistService persistService, LocationService locationService, GeoTree geoTree, HttpServerOptions httpOptions,
			DefaultChannelPipelineFactory httpPipelineFactory, RawSocketChannelPipelineFactory rawSocketpipelineFactory) {
		super();
		this.persistService = persistService;
		this.locationService = locationService;
		this.geoTree = geoTree;
		this.httpOptions = httpOptions;
		this.httpPipelineFactory = httpPipelineFactory;
		this.rawSocketpipelineFactory = rawSocketpipelineFactory;
	}

	public void start() {
		startCoreServices();
		bindWithPipeline(this.httpPipelineFactory, httpOptions.port());
		bindWithPipeline(this.rawSocketpipelineFactory, httpOptions.port() + 1);
	}

	private void bindWithPipeline(ChannelPipelineFactory pipelineFactory, int port) {
		final ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(pipelineFactory);
		bootstrap.bind(new InetSocketAddress(port));
		logger.info("Binding pipeline " + pipelineFactory + "on port " + port);
	}

	private void startCoreServices() {
		logger.info("Starting core services");
		this.persistService.start();
		this.locationService.start();
		this.geoTree.start();
		logger.info("Core services started");
	}

}
