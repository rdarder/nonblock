package com.globant.nonblock.netty.server.pipeline;

import static org.jboss.netty.channel.Channels.pipeline;

import javax.inject.Inject;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.globant.nonblock.netty.server.pipeline.handler.UriBasedPipelineSwitcher;
import com.google.inject.Provider;

public class DefaultChannelPipelineFactory implements ChannelPipelineFactory {

	final Provider<UriBasedPipelineSwitcher> provider;

	@Inject
	public DefaultChannelPipelineFactory(final Provider<UriBasedPipelineSwitcher> provider) {
		super();
		this.provider = provider;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		final ChannelPipeline pipeline = pipeline();
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("switcher", provider.get());
		return pipeline;
	}

}
