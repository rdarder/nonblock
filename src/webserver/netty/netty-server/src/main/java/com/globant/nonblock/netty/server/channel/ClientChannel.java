package com.globant.nonblock.netty.server.channel;

/**
 * @author Julian Gutierrez Oschmann
 *
 */
public interface ClientChannel {

	void write(String message);
	
}
