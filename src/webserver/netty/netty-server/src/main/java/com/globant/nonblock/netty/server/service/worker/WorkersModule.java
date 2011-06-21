package com.globant.nonblock.netty.server.service.worker;

import com.google.inject.AbstractModule;

public class WorkersModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(WorkerThreadPool.class).asEagerSingleton();
		bind(DirtyNodesQueue.class).asEagerSingleton();
	}

}
