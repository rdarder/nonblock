package com.globant.nonblock.netty.server.service.votes;

import com.globant.nonblock.netty.server.message.loader.VotesResult;
import com.globant.nonblock.netty.server.message.newdata.NewDataMessage;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;

public interface VoteService {

	void addVotes(VotesResult newResults);

	NewDataMessage calculateStatus(SubscribeMessage message);

}
