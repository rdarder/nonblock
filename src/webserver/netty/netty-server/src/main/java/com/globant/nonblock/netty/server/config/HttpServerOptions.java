package com.globant.nonblock.netty.server.config;

import com.google.sitebricks.options.Options;

@Options("http")
public abstract class HttpServerOptions {

	/**
	 * Fallback http port.
	 * 
	 * @return the default port number.
	 */
	public int port() {
		return 9090;
	}
	
}
