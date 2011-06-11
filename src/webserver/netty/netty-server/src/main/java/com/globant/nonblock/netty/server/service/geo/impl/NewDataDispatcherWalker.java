package com.globant.nonblock.netty.server.service.geo.impl;

import com.globant.nonblock.netty.server.channel.BroadcastClientChannelSet;
import com.globant.nonblock.netty.server.message.newdata.NewDataMessage;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;
import com.globant.nonblock.netty.server.service.geo.GeoTreeWalker;
import com.globant.nonblock.netty.server.service.votes.VoteService;
import com.google.inject.Inject;

public class NewDataDispatcherWalker implements GeoTreeWalker {

	private final VoteService voteService;

	@Inject
	public NewDataDispatcherWalker(final VoteService voteService) {
		super();
		this.voteService = voteService;
	}

	@Override
	public void visit(final ChannelGroupGeoNode geoNode) {
		for (final SubscribeMessage sm : geoNode.getAllSubscriberMessages()) {
			final BroadcastClientChannelSet channelGroup = geoNode.getChannelGroup(sm);
			final NewDataMessage message = this.voteService.calculateStatus(sm);
			channelGroup.writeToAll(message.toJson() + "\n");
		}
	}

	@Override
	public boolean childFirst() {
		return true;
	}

}
