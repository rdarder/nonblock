package com.globant.nonblock.netty.server.service.votes;

import java.util.List;

import com.globant.nonblock.netty.server.message.loader.SubmitVotesMessage;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;

public interface VoteService {

	void addVotes(SubmitVotesMessage newResults);

	List<Object[]> calculateStatus(SubscribeMessage message);

}
