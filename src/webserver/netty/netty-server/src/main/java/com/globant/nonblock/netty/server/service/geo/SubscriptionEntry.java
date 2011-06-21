package com.globant.nonblock.netty.server.service.geo;

import com.globant.nonblock.netty.server.channel.ClientChannel;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;

public class SubscriptionEntry {
	public ClientChannel clientChannel;
	public SubscribeMessage originalSubscribeMessage;
}