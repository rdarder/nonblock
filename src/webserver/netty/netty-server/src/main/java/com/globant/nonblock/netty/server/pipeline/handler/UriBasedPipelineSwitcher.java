package com.globant.nonblock.netty.server.pipeline.handler;

import javax.inject.Inject;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

import com.globant.nonblock.netty.server.pipeline.handler.request.NewDataPostHandler;
import com.globant.nonblock.netty.server.pipeline.handler.resource.HttpStaticFileServerHandler;
import com.globant.nonblock.netty.server.pipeline.handler.websocket.WebSocketMessageEncoderHandler;
import com.globant.nonblock.netty.server.pipeline.handler.websocket.WebSocketServerHandler;
import com.google.inject.Provider;

public class UriBasedPipelineSwitcher extends SimpleChannelUpstreamHandler {

	private Provider<MessageManagerHandler> messageBrokerHanlderProvider;
	private Provider<NewDataPostHandler> newDataPostHandlerProvider;

	@Inject
	public UriBasedPipelineSwitcher(Provider<MessageManagerHandler> messageBrokerHanlderProvider, Provider<NewDataPostHandler> newDataPostHandlerProvider) {
		super();
		this.messageBrokerHanlderProvider = messageBrokerHanlderProvider;
		this.newDataPostHandlerProvider = newDataPostHandlerProvider;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		HttpRequest httpMessage = (HttpRequest) e.getMessage();

		if (httpMessage.getUri().startsWith("/ws")) {
			System.out.println("Serving web sockets");
			ChannelPipeline p = ctx.getPipeline();
			p.addLast("webSocketHandler", new WebSocketServerHandler());
			p.addLast("messageEncoder", new WebSocketMessageEncoderHandler());
			p.addLast("messageBroker", this.messageBrokerHanlderProvider.get());
			p.remove(this);
			System.out.println("");
		} else if (httpMessage.getUri().startsWith("/static")) {
			System.out.println("Serving static resources");
			ChannelPipeline p = ctx.getPipeline();
			p.addLast("chunkedWriter", new ChunkedWriteHandler());
			p.addLast("staticFilesHandler", new HttpStaticFileServerHandler());
			p.remove(this);
		} else if (httpMessage.getUri().startsWith("/post")) {
			System.out.println("Serving post service");
			ChannelPipeline p = ctx.getPipeline();
			p.addLast("postMessageBroker", newDataPostHandlerProvider.get());
			p.remove(this);
		}

		ctx.sendUpstream(e);

	}

}
