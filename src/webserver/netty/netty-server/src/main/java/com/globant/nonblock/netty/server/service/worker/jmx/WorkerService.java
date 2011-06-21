package com.globant.nonblock.netty.server.service.worker.jmx;

import javax.inject.Inject;

import com.globant.nonblock.netty.server.service.worker.DirtyNodesQueue;

public class WorkerService implements WorkerServiceMBean {

	private final DirtyNodesQueue dirtyNodesQueue;

	@Inject
	public WorkerService(DirtyNodesQueue dirtyNodesQueue) {
		super();
		this.dirtyNodesQueue = dirtyNodesQueue;
	}

	@Override
	public int getQueueSize() {
		return 1000;
	}

	@Override
	public int getQueueState() {
		return this.dirtyNodesQueue.getDirtyTreeNodesQueue().size();
	}

}
