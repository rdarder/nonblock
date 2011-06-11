package com.globant.nonblock.netty.server.pipeline.handler.config;

import com.google.sitebricks.options.Options;

@Options("url")
public abstract class URLHandlerOptions {

	public String webSocketUrl() {
		return "ws";
	}
	
	public String appUrl() {
		return "app";
	}
	
	public String loadServiceUrl() {
		return "loadVotes";
	}
	
}
