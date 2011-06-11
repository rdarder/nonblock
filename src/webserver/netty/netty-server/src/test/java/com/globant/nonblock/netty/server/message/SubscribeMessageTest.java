package com.globant.nonblock.netty.server.message;

import org.junit.Test;

import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SubscribeMessageTest extends TestCase {

	private final MessageParser mp = new MessageParser();
	
	@Test
	public void testSubscribeMessage() {
		
		final String msg = "	{ " +
		"					event : 'subscribe', " +
		"					data : { " +
		"						puesto : 'Intendente', " + 
		"						alcance : 'Provincia', " +
		"						valor_alcance : 'Santa Fe'," +
		"						nivel : 'Localidad'" +
		"					}" +
		"				}";
		
		ClientMessage clientMessage = mp.parseClientMessage(msg);
		
		Assert.assertTrue(clientMessage instanceof SubscribeMessage);		
		
		SubscribeMessage sm = (SubscribeMessage) clientMessage;
		
		Assert.assertEquals(sm.getAlcanceValue(), "Santa Fe");
		Assert.assertEquals(sm.getPuesto(), "Intendente");
		
	}
	
}
