package com.globant.nonblock.netty.server.pipeline.handler;

import javax.inject.Inject;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.globant.nonblock.netty.server.pipeline.handler.config.URLHandlerOptions;
import com.globant.nonblock.netty.server.pipeline.handler.request.NewDataPostHandler;
import com.globant.nonblock.netty.server.pipeline.handler.resource.HttpStaticFileServerHandler;
import com.globant.nonblock.netty.server.pipeline.handler.websocket.WebSocketMessageEncoderHandler;
import com.globant.nonblock.netty.server.pipeline.handler.websocket.WebSocketServerHandler;
import com.google.inject.Provider;

public class UriBasedPipelineSwitcher extends SimpleChannelUpstreamHandler {

	private final Provider<MessageManagerHandler> messageBrokerHanlderProvider;
	private final Provider<NewDataPostHandler> newDataPostHandlerProvider;
	private final Provider<HttpStaticFileServerHandler> staticContentHanlderProvider;
	private final URLHandlerOptions urlHandlerOptions;
	
	private final static Logger logger = LoggerFactory.getLogger(UriBasedPipelineSwitcher.class);
	
	@Inject
	public UriBasedPipelineSwitcher(Provider<MessageManagerHandler> messageBrokerHanlderProvider, Provider<NewDataPostHandler> newDataPostHandlerProvider,
			Provider<HttpStaticFileServerHandler> staticContentHandlerProvider, URLHandlerOptions urlHandlerOptions) {
		super();
		this.messageBrokerHanlderProvider = messageBrokerHanlderProvider;
		this.newDataPostHandlerProvider = newDataPostHandlerProvider;
		this.staticContentHanlderProvider = staticContentHandlerProvider;
		this.urlHandlerOptions = urlHandlerOptions;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		HttpRequest httpMessage = (HttpRequest) e.getMessage();

		logger.info("Procesing http request at URI " + httpMessage.getUri());
		
		if (httpMessage.getUri().startsWith("/" + urlHandlerOptions.webSocketUrl())) {
			final ChannelPipeline p = ctx.getPipeline();
			p.addLast("webSocketHandler", new WebSocketServerHandler());
			p.addLast("messageEncoder", new WebSocketMessageEncoderHandler());
			p.addLast("messageBroker", messageBrokerHanlderProvider.get());
			p.remove(this);
		} else if (httpMessage.getUri().startsWith("/" + urlHandlerOptions.appUrl())) {
			final ChannelPipeline p = ctx.getPipeline();
			p.addLast("chunkedWriter", new ChunkedWriteHandler());
			p.addLast("staticFilesHandler", staticContentHanlderProvider.get());
			p.remove(this);
		} else if (httpMessage.getUri().startsWith("/" + urlHandlerOptions.loadServiceUrl())) {
			final ChannelPipeline p = ctx.getPipeline();
			p.addLast("postMessageBroker", newDataPostHandlerProvider.get());
			p.remove(this);
		} else {
			//TODO error handler
		}

		ctx.sendUpstream(e);

	}

}
