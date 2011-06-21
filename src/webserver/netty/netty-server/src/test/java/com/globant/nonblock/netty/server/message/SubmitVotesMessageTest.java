package com.globant.nonblock.netty.server.message;

import org.junit.Test;

import com.globant.nonblock.netty.server.message.loader.SubmitVotesMessage;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SubmitVotesMessageTest extends TestCase {

	@Test
	public void testSubmitVotesMessage() {
		
		final String msg = "{ 						" +
		"		name: 'submitVotes', 				" +
		"		id: 10,								" +
		"		ref: 20,							" +
		"		data: { 							" +
		"			mesa: '758',					" +
		"			votos: [{						" +
		"				puesto:	'intendente',		" +
		"				candidato:	'Giustiniani',	" +
		"				partido: 'PSD',				" +
		"				cant:	7 					" +
		"			}]								" +
		"		}}									"	;
	
		SubmitVotesMessage message = new SubmitVotesMessage();
		message.fromJson(msg);
		
		Assert.assertEquals("submitVotes", message.getName());
		Assert.assertEquals(Integer.valueOf(10), message.getId());
		Assert.assertEquals(Integer.valueOf(20), message.getRef());
		
		Assert.assertEquals(1, message.getData().size());
	}
}
