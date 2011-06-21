package com.globant.nonblock.netty.server.pipeline.handler.resource.config;

import com.google.sitebricks.options.Options;

@Options("static")
public abstract class StaticContentOptions {

	public String rootPath() {
		return System.getProperty("user.dir");
	}
	
}
