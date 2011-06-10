package com.globant.nonblock.netty.server.service.geo;

import com.globant.nonblock.netty.server.channel.ClientChannel;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;


public interface GeoNode {

	void addSubscriptor(SubscribeMessage message, ClientChannel channel);

	void traverse(GeoTreeWalker geoTreeVisitor);
}
