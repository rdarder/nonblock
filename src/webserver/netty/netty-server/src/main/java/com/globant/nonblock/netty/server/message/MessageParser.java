package com.globant.nonblock.netty.server.message;

import net.sf.json.JSONObject;

import com.globant.nonblock.netty.server.message.binding.Message;
import com.globant.nonblock.netty.server.message.loader.SubmitVotesMessage;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;

public class MessageParser {

	public Message parseClientMessage(final String jsonMessage) {

		final JSONObject rootMap = JSONObject.fromObject(jsonMessage);
		final String messageName = (String) rootMap.get("name");

		if (messageName.equals("subscribe")) {
			SubscribeMessage message = new SubscribeMessage();
			message.fromJson(jsonMessage);
			return message;
		} else if (messageName.equals("submitVotes")) {
			SubmitVotesMessage message = new SubmitVotesMessage();
			message.fromJson(jsonMessage);
			return message;
		}
		throw new IllegalArgumentException("Bad message type: " + messageName);
	}
	
}
