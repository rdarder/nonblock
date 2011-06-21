package com.globant.nonblock.netty.server.log.event;

import com.globant.nonblock.netty.server.message.loader.SubmitVotesMessage;

public class SubmitVotesResponseEvent implements Event {

	private SubmitVotesMessage message;
	
	public SubmitVotesResponseEvent(final SubmitVotesMessage message) {
		super();
		this.message = message;
	}

	@Override
	public String getMessage() {
		return "votes_proccesed," + System.currentTimeMillis() + "," + message.getId();
	}

}
