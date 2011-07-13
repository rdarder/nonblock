package com.globant.nonblock.netty.server.log.event;

import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;

public class SubscribeReceivedEvent implements Event {

	private final SubscribeMessage message;

	public SubscribeReceivedEvent(SubscribeMessage message) {
		super();
		this.message = message;
	}

	@Override
	public String[] getMessages() {
		return new String[] { "subscribeReceived," + System.currentTimeMillis() + ",," + message.getId() + ","};
	}

}
