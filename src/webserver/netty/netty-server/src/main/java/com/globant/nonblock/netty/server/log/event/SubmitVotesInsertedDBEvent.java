package com.globant.nonblock.netty.server.log.event;

import com.globant.nonblock.netty.server.message.loader.SubmitVotesMessage;

public class SubmitVotesInsertedDBEvent implements Event {

	private final SubmitVotesMessage message;

	public SubmitVotesInsertedDBEvent(final SubmitVotesMessage message) {
		super();
		this.message = message;
	}

	@Override
	public String[] getMessages() {
		return new String[] {  "submitVotesInsertedDB," + System.currentTimeMillis() + ",," + message.getId() + "," };
	}

}
