package com.globant.nonblock.netty.server.message;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;
import com.globant.nonblock.netty.server.service.location.LocationType;

public class SubscribeMessageTest extends TestCase {

	@Test
	public void testSubscribeMessage() {
		
		final String msg = "{ 								" +
		"					name : 'subscribe', 			" +
		"					id : 1, 						" +
		"					ref: 2, 						" +		
		"					data : { 						" +
		"						puesto : 'Intendente', 		" + 
		"						alcance : 'Provincia', 		" +
		"						lugar : 'Santa Fe',	" +
		"						nivel : 'Localidad'			" +
		"					}								" +
		"					}								";
		
		SubscribeMessage m = new SubscribeMessage(21, 1);

		m.fromJson(msg);
		
		Assert.assertEquals(m.getName(), "subscribe");
		Assert.assertEquals(m.getId(), Integer.valueOf(1));
		Assert.assertEquals(m.getRef(), Integer.valueOf(2));

		Assert.assertEquals("Santa Fe", m.getLugar() );
		Assert.assertEquals(LocationType.Localidad, m.getNivel() );
		Assert.assertEquals("Intendente",m.getPuesto());
		Assert.assertEquals(LocationType.Provincia,m.getAlcance());
		
	}
	
}
