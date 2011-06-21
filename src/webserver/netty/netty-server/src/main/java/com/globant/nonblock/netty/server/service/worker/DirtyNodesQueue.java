package com.globant.nonblock.netty.server.service.worker;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.globant.nonblock.netty.server.service.geo.GeoNode;

public class DirtyNodesQueue {

	private final BlockingQueue<GeoNode> dirtyTreeNodesQueue = new ArrayBlockingQueue<GeoNode>(1000);

	public BlockingQueue<GeoNode> getDirtyTreeNodesQueue() {
		return dirtyTreeNodesQueue;
	}

}
