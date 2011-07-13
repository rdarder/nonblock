package com.globant.nonblock.netty.server.log.event;

import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;

public class NewDataSendEvent implements Event {

	private final Long transactionId;

	private final SubscribeMessage message;

	public NewDataSendEvent(Long transactionId, SubscribeMessage message) {
		super();
		this.transactionId = transactionId;
		this.message = message;
	}

	@Override
	public String[] getMessages() {
		return new String[] { "newDataSend," + System.currentTimeMillis() + "," + this.transactionId + ",," + message.getId()};
	}

}
