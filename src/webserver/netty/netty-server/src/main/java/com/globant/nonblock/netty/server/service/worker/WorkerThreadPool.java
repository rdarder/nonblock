package com.globant.nonblock.netty.server.service.worker;

import javax.inject.Inject;

import com.google.inject.Provider;

public class WorkerThreadPool {

	private final Provider<DirtyNodesUpdaterTask> updaterTask;

	private final Integer threadNumber = 20;
	
	@Inject
	public WorkerThreadPool(final Provider<DirtyNodesUpdaterTask> updaterTask) {
		super();
		this.updaterTask = updaterTask;

		for (int i = 1; i <= this.threadNumber; i++) {
			new Thread(this.updaterTask.get(), "DirtyNodesUpdater" + i).start();			
		}
	}

}
