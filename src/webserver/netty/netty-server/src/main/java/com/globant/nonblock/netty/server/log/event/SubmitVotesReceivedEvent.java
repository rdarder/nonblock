package com.globant.nonblock.netty.server.log.event;

import com.globant.nonblock.netty.server.message.loader.SubmitVotesMessage;

public class SubmitVotesReceivedEvent implements Event {

	private final SubmitVotesMessage message;

	public SubmitVotesReceivedEvent(final SubmitVotesMessage message) {
		super();
		this.message = message;
	}

	@Override
	public String getMessage() {
		return "votes_received," + System.currentTimeMillis() + "," + message.getId(); 
	}
	
}
