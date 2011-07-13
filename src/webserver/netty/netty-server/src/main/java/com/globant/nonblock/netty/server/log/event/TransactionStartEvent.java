package com.globant.nonblock.netty.server.log.event;

import java.util.Collection;

import com.globant.nonblock.netty.server.message.loader.SubmitVotesMessage;

public class TransactionStartEvent implements Event {

	private final Collection<SubmitVotesMessage> messages;

	private final Long transactionId;
	
	public TransactionStartEvent(final Long transactionId, final Collection<SubmitVotesMessage> messages) {
		super();
		this.messages = messages;
		this.transactionId = transactionId;
	}

	@Override
	public String[] getMessages() {
		final String[] messages = new String[this.messages.size()];
		int p = 0;
		for (SubmitVotesMessage message : this.messages) {
			messages[p++] = "transaction," + System.currentTimeMillis() + "," + this.transactionId + ",," + message.getId();
		}
		return messages;
	}

}
