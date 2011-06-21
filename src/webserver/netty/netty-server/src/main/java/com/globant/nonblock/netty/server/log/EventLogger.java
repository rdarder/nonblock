package com.globant.nonblock.netty.server.log;

import com.globant.nonblock.netty.server.log.event.Event;

public interface EventLogger {

	void process(Event event);

}
