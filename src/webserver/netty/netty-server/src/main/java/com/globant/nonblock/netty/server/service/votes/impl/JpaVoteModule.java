package com.globant.nonblock.netty.server.service.votes.impl;

import com.globant.nonblock.netty.server.service.votes.VoteService;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class JpaVoteModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(VoteService.class).to(JpaVoteServiceImpl.class).in(Singleton.class);
	}

}
