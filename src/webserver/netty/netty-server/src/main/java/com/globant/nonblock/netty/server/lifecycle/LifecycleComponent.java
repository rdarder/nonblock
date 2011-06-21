package com.globant.nonblock.netty.server.lifecycle;

/**
 * A server component with a managed lifecycle.
 * At bootstrap, the server starts all managed components calling {@link #start()} <br>
 * On shutdown, the server calls {@link #shutshown()} method.
 * 
 * @author Julian Gutierrez Oschmann
 *
 */
public interface LifecycleComponent {

	/**
	 * Start the component.
	 */
	void start();

	/**
	 * Stop the component.
	 */
	void shutshown();

}
