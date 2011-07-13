package com.globant.nonblock.netty.server.service.worker;

import com.globant.nonblock.netty.server.service.worker.jmx.WorkerService;
import com.globant.nonblock.netty.server.service.worker.jmx.WorkerServiceMBean;
import com.google.inject.AbstractModule;

public class WorkersModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(WorkerThreadPool.class).asEagerSingleton();
		bind(DirtyNodesQueue.class).asEagerSingleton();
		bind(WorkerServiceMBean.class).to(WorkerService.class);
	}

}
