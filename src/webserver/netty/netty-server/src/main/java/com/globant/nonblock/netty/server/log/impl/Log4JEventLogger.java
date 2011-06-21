package com.globant.nonblock.netty.server.log.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.globant.nonblock.netty.server.log.EventLogger;
import com.globant.nonblock.netty.server.log.event.Event;

public class Log4JEventLogger implements EventLogger {

	private final static Logger logger = LoggerFactory.getLogger("events");
	
	@Override
	public void process(final Event event) {
		logger.info(event.getMessage());
	}

}
