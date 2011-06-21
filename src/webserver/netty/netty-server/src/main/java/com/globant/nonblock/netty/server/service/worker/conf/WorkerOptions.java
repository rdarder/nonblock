package com.globant.nonblock.netty.server.service.worker.conf;

import com.google.sitebricks.options.Options;

@Options("worker")
public abstract class WorkerOptions {

	public Long sleep() {
		return 0L;
	}

}
