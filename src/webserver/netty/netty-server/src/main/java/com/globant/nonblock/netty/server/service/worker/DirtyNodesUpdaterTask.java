package com.globant.nonblock.netty.server.service.worker;

import java.util.List;

import javax.inject.Inject;

import com.globant.nonblock.netty.server.message.newdata.NewDataMessage;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;
import com.globant.nonblock.netty.server.service.geo.GeoNode;
import com.globant.nonblock.netty.server.service.geo.SubscriptionEntry;
import com.globant.nonblock.netty.server.service.mapping.NewDataMessageBuilder;
import com.globant.nonblock.netty.server.service.votes.VoteService;
import com.globant.nonblock.netty.server.service.worker.conf.WorkerOptions;

public class DirtyNodesUpdaterTask implements Runnable {

	private final VoteService voteService;

	private final DirtyNodesQueue dirtyNodesQueue;

	private final WorkerOptions options;

	@Inject
	public DirtyNodesUpdaterTask(final VoteService voteService, final DirtyNodesQueue dirtyNodesQueue, final WorkerOptions options) {
		super();
		this.voteService = voteService;
		this.dirtyNodesQueue = dirtyNodesQueue;
		this.options = options;
	}

	@Override
	public void run() {
		try {
			while (true) {
				GeoNode n = this.dirtyNodesQueue.getDirtyTreeNodesQueue().take();
				synchronized (n) {
					if (n.isDirty()) {
						processNode(n);
					}
					n.clean();
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

	}

	private void processNode(final GeoNode geoNode) {
		for (final SubscribeMessage sm : geoNode.getAllSubscriberMessages()) {
			List<Object[]> result = this.voteService.calculateStatus(sm);
			for (SubscriptionEntry se : geoNode.getChannelGroup(sm)) {
				NewDataMessage newDataMsg = NewDataMessageBuilder.buildFromQuery(result, se.originalSubscribeMessage);
				se.clientChannel.write(newDataMsg.toJson() + "\n");
			}
		}
	}

}
