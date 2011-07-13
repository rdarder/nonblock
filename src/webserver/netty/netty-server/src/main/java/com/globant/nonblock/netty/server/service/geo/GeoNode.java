package com.globant.nonblock.netty.server.service.geo;

import java.util.Collection;
import java.util.Set;

import com.globant.nonblock.netty.server.channel.ClientChannel;
import com.globant.nonblock.netty.server.message.loader.SubmitVotesMessage;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;

public interface GeoNode {

	void addSubscriptor(SubscribeMessage message, ClientChannel channel);

	void traverse(GeoTreeWalker geoTreeVisitor);

	Set<SubscribeMessage> getAllSubscriberMessages();

	Set<SubscriptionEntry> getChannelGroup(SubscribeMessage message);

	void setDirty();

	void clean();

	boolean isDirty();
	
	void addSubmitVoteMessage(SubmitVotesMessage message);
	
	Collection<SubmitVotesMessage> getAllMessages();
}
