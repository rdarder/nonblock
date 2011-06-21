package com.globant.nonblock.netty.server.pipeline.handler;

import javax.inject.Inject;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.jboss.netty.util.CharsetUtil;
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
	private final ExecutionHandler executionHandler;
	private final URLHandlerOptions urlHandlerOptions;
	
	private final static Logger logger = LoggerFactory.getLogger(UriBasedPipelineSwitcher.class);
	
	@Inject
	public UriBasedPipelineSwitcher(Provider<MessageManagerHandler> messageBrokerHanlderProvider, Provider<NewDataPostHandler> newDataPostHandlerProvider,
			Provider<HttpStaticFileServerHandler> staticContentHandlerProvider, URLHandlerOptions urlHandlerOptions, ExecutionHandler executionHandler) {
		super();
		this.messageBrokerHanlderProvider = messageBrokerHanlderProvider;
		this.newDataPostHandlerProvider = newDataPostHandlerProvider;
		this.staticContentHanlderProvider = staticContentHandlerProvider;
		this.urlHandlerOptions = urlHandlerOptions;
		this.executionHandler = executionHandler;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		final HttpRequest httpMessage = (HttpRequest) e.getMessage();
		final String uri = httpMessage.getUri();

		logger.info("Procesing http request at URI " + uri);

		if (uri.startsWith("/" + urlHandlerOptions.webSocketUrl())) {
			final ChannelPipeline p = ctx.getPipeline();
			p.addLast("webSocketHandler", new WebSocketServerHandler());
			p.addLast("messageEncoder", new WebSocketMessageEncoderHandler());
			p.addLast("messageBroker", messageBrokerHanlderProvider.get());
			p.remove(this);
		} else if (uri.startsWith("/" + urlHandlerOptions.appUrl())) {
			final ChannelPipeline p = ctx.getPipeline();
			p.addLast("chunkedWriter", new ChunkedWriteHandler());
			p.addLast("staticFilesHandler", staticContentHanlderProvider.get());
			p.remove(this);
		} else if (uri.startsWith("/" + urlHandlerOptions.loadServiceUrl())) {
			final ChannelPipeline p = ctx.getPipeline();
			p.addLast("executionHanlder", executionHandler);
			p.addLast("postMessageBroker", newDataPostHandlerProvider.get());
			p.remove(this);
		} else {
			writeNotFoundResponse(ctx, uri);
		}

		ctx.sendUpstream(e);

	}

	private void writeNotFoundResponse(final ChannelHandlerContext ctx, final String uri) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer(
                "Resource " + uri + " not found",
                CharsetUtil.UTF_8));
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
		
	}

}
