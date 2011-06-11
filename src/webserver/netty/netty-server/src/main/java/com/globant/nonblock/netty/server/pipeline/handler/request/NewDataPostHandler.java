package com.globant.nonblock.netty.server.pipeline.handler.request;

import javax.inject.Inject;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.globant.nonblock.netty.server.message.ClientMessage;
import com.globant.nonblock.netty.server.message.MessageParser;
import com.globant.nonblock.netty.server.message.loader.VotesResult;
import com.globant.nonblock.netty.server.service.geo.GeoNode;
import com.globant.nonblock.netty.server.service.geo.GeoTree;
import com.globant.nonblock.netty.server.service.geo.impl.NewDataDispatcherWalker;
import com.globant.nonblock.netty.server.service.location.LocationType;
import com.globant.nonblock.netty.server.service.votes.VoteService;

public class NewDataPostHandler extends SimpleChannelUpstreamHandler {

	private final GeoTree geoTree;
	private final VoteService voteService;
	private final MessageParser messageParser = new MessageParser();

	@Inject
	public NewDataPostHandler(final GeoTree geoTree, final VoteService e) {
		this.geoTree = geoTree;
		this.voteService = e;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

		final String postContent = readPostContent(e);
		final ClientMessage cm = parseMessage(postContent);

		if (cm != null && cm instanceof VotesResult) {
			VotesResult vr = (VotesResult) cm;
			this.voteService.addVotes(vr);
			GeoNode gn = this.geoTree.findGeoNode(LocationType.Mesa, vr.getMesa());
			gn.traverse(new NewDataDispatcherWalker(voteService));
		}
		
		e.getChannel().close();
	}

	private ClientMessage parseMessage(final String postContent) {
		try {
			final ClientMessage cm = this.messageParser.parseClientMessage(postContent);
			return cm;
		} catch (Exception e) {
			return null;
		}
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
