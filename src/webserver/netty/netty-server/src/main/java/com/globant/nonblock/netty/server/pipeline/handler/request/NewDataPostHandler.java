package com.globant.nonblock.netty.server.pipeline.handler.request;

import javax.inject.Inject;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.globant.nonblock.netty.server.log.EventLogger;
import com.globant.nonblock.netty.server.log.event.SubmitVotesReceivedEvent;
import com.globant.nonblock.netty.server.log.event.SubmitVotesResponseEvent;
import com.globant.nonblock.netty.server.message.MessageParser;
import com.globant.nonblock.netty.server.message.binding.Message;
import com.globant.nonblock.netty.server.message.loader.SubmitVotesMessage;
import com.globant.nonblock.netty.server.service.geo.GeoNode;
import com.globant.nonblock.netty.server.service.geo.GeoTree;
import com.globant.nonblock.netty.server.service.geo.impl.NewDataDispatcherWalker;
import com.globant.nonblock.netty.server.service.location.LocationType;
import com.globant.nonblock.netty.server.service.votes.VoteService;
import com.google.inject.Provider;

public class NewDataPostHandler extends SimpleChannelUpstreamHandler {

	private final GeoTree geoTree;
	private final VoteService voteService;
	private final MessageParser messageParser = new MessageParser();
	private final Provider<NewDataDispatcherWalker> walkerProvider;
	private final EventLogger eventLogger;

	@Inject
	public NewDataPostHandler(GeoTree geoTree, VoteService voteService, Provider<NewDataDispatcherWalker> walkerProvider, EventLogger eventLogger) {
		super();
		this.geoTree = geoTree;
		this.voteService = voteService;
		this.walkerProvider = walkerProvider;
		this.eventLogger = eventLogger;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

		final String postContent = readPostContent(e);
		final Message cm = parseMessage(postContent);

		if (cm != null && cm instanceof SubmitVotesMessage) {
			
			SubmitVotesMessage vr = (SubmitVotesMessage) cm;
			this.eventLogger.process(new SubmitVotesReceivedEvent(vr));
			processVotesResult(vr);
			writeOkAndClose(ctx, vr);
		} else {
			// Only SubmitVotesMessage allowed.
			writeErrorAndClose(ctx);			
		}
	}

	private void processVotesResult(final SubmitVotesMessage vr) {
		this.voteService.addVotes(vr);
		GeoNode gn = this.geoTree.findGeoNode(LocationType.Mesa, vr.getMesa());
		gn.traverse(this.walkerProvider.get());
	}

	private Message parseMessage(final String postContent) {
		try {
			final Message cm = this.messageParser.parseClientMessage(postContent);
			return cm;
		} catch (Exception e) {
			return null;
		}
	}

	private void writeOkAndClose(final ChannelHandlerContext ctx, final SubmitVotesMessage cm) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");

		ctx.getChannel().getCloseFuture().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture future) throws Exception {
				NewDataPostHandler.this.eventLogger.process(new SubmitVotesResponseEvent(cm));
			}
		});
		ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
	}
	
	private void writeErrorAndClose(final ChannelHandlerContext ctx) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
	}

	private String readPostContent(final MessageEvent e) {
		final HttpRequest request = (HttpRequest) e.getMessage();
		final long l = HttpHeaders.getContentLength(request);
		final byte[] postContentBuffer = new byte[Long.valueOf(l).intValue()];
		request.getContent().readBytes(postContentBuffer);
		final String postContent = new String(postContentBuffer);
		return postContent;
	}

}
