package com.globant.nonblock.netty.server.pipeline;

import static org.jboss.netty.channel.Channels.pipeline;

import java.nio.charset.Charset;

import javax.inject.Inject;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import com.globant.nonblock.netty.server.pipeline.handler.MessageManagerHandler;
import com.google.inject.Provider;

public class RawSocketChannelPipelineFactory implements ChannelPipelineFactory {

	private Provider<MessageManagerHandler> messageManagerHandlerFactory;

	@Inject
	public RawSocketChannelPipelineFactory(Provider<MessageManagerHandler> messageBrokerHanlderProvider) {
		super();
		this.messageManagerHandlerFactory = messageBrokerHanlderProvider;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		final ChannelPipeline pipeline = pipeline();
		pipeline.addLast("stringDecoder", new StringDecoder(Charset.forName("UTF-8")));
		pipeline.addLast("stringEncoder", new StringEncoder(Charset.forName("UTF-8")));
		pipeline.addLast("encoder", this.messageManagerHandlerFactory.get());
		return pipeline;
	}

}
