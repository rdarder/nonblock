package com.globant.nonblock.netty.server.service.geo.impl;

import com.globant.nonblock.netty.server.service.geo.GeoNode;
import com.globant.nonblock.netty.server.service.geo.GeoTreeWalker;
import com.globant.nonblock.netty.server.service.worker.DirtyNodesQueue;
import com.google.inject.Inject;

public class NewDataDispatcherWalker implements GeoTreeWalker {

	private final DirtyNodesQueue dirtyNodesQueue;

	@Inject
	public NewDataDispatcherWalker(final DirtyNodesQueue dirtyNodesQueue) {
		super();
		this.dirtyNodesQueue = dirtyNodesQueue;
	}

	@Override
	public void visit(final GeoNode geoNode) {
		synchronized (geoNode) {
			geoNode.setDirty();
			dirtyNodesQueue.getDirtyTreeNodesQueue().add(geoNode);			
		}
	}

	@Override
	public boolean childFirst() {
		return true;
	}

}
