package com.globant.nonblock.netty.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.globant.nonblock.netty.server.config.HttpServerOptions;
import com.globant.nonblock.netty.server.pipeline.DefaultChannelPipelineFactory;
import com.globant.nonblock.netty.server.pipeline.RawSocketChannelPipelineFactory;
import com.globant.nonblock.netty.server.service.geo.GeoTree;
import com.globant.nonblock.netty.server.service.location.LocationService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.persist.PersistService;
import com.google.sitebricks.options.OptionsModule;

public class MainServer {

	public static void main(String[] cmdLine) {

		Module optionsModule = new OptionsModule(cmdLine).options(HttpServerOptions.class);

		Injector mainInjector = Guice.createInjector(new MainServerModule(), optionsModule);

		mainInjector.getInstance(PersistService.class).start();
		mainInjector.getInstance(LocationService.class).start();
		mainInjector.getInstance(GeoTree.class).start();

		HttpServerOptions httpOptions = mainInjector.getInstance(HttpServerOptions.class);

		final ChannelPipelineFactory pipelineFactory = mainInjector.getInstance(DefaultChannelPipelineFactory.class);

		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(pipelineFactory);
		bootstrap.bind(new InetSocketAddress(httpOptions.port()));

		ServerBootstrap bootstrap2 = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap2.setPipelineFactory(mainInjector.getInstance(RawSocketChannelPipelineFactory.class));
		bootstrap2.bind(new InetSocketAddress(httpOptions.port() + 1));

	}
}
