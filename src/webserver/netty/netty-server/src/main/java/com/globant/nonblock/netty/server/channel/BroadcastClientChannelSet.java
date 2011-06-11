package com.globant.nonblock.netty.server.channel;

import java.util.Set;

public interface BroadcastClientChannelSet extends Set<ClientChannel> {

	void writeToAll(String message);

}
