package com.globant.nonblock.netty.server.service.worker;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import com.globant.nonblock.netty.server.log.EventLogger;
import com.globant.nonblock.netty.server.log.event.NewDataSendEvent;
import com.globant.nonblock.netty.server.log.event.TransactionStartEvent;
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

	private final EventLogger eventLogger;
	
	private static final AtomicLong txCount = new AtomicLong();

	@Inject
	public DirtyNodesUpdaterTask(final VoteService voteService, final DirtyNodesQueue dirtyNodesQueue, final WorkerOptions options, final EventLogger eventLogger) {
		super();
		this.voteService = voteService;
		this.dirtyNodesQueue = dirtyNodesQueue;
		this.options = options;
		this.eventLogger = eventLogger;
	}

	@Override
	public void run() {
		try {
			while (true) {
				final GeoNode n = this.dirtyNodesQueue.getDirtyTreeNodesQueue().take();
				synchronized (n) {
					if (n.isDirty()) {
						processNode(n);
					}
					n.clean();
				}
			}
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}

	}

	private void processNode(final GeoNode geoNode) {
		
		if (!geoNode.getAllSubscriberMessages().isEmpty()) {
			final Long txNumber = txCount.getAndIncrement();
			this.eventLogger.process(new TransactionStartEvent(txNumber, geoNode.getAllMessages()));
			for (final SubscribeMessage sm : geoNode.getAllSubscriberMessages()) {
				List<Object[]> result = this.voteService.calculateStatus(sm);
				for (final SubscriptionEntry se : geoNode.getChannelGroup(sm)) {
					NewDataMessage newDataMsg = NewDataMessageBuilder.buildFromQuery(result, se.originalSubscribeMessage);
					se.clientChannel.write(newDataMsg.toJson() + "\n");
					this.eventLogger.process(new NewDataSendEvent(txNumber, se.originalSubscribeMessage));
				}
			}				
		}
	}

}
