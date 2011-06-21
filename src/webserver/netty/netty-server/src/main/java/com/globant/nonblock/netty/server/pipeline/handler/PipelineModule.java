package com.globant.nonblock.netty.server.pipeline.handler;

import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import com.google.inject.AbstractModule;

public class PipelineModule extends AbstractModule {

	@Override
	protected void configure() {
		 ExecutionHandler executionHandler = new ExecutionHandler(
	             new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576));
		bind(ExecutionHandler.class).toInstance(executionHandler);
	}

}
