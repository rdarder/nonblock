package com.globant.nonblock.netty.server.message;

import junit.framework.TestCase;

import org.junit.Test;

import com.globant.nonblock.netty.server.message.newdata.NewDataDatum;
import com.globant.nonblock.netty.server.message.newdata.NewDataMessage;

public class NewDataMessageTest extends TestCase {

	@Test
	public void testNewDataRendering() {
		NewDataMessage msg = new NewDataMessage(1,3);

		for (int i = 0; i < 10; i++) {
			msg.getDatos().add(new NewDataDatum("puesto"+ i, "mesa" + i, "local"+i, "seccional" + i,
					"localidad"+i, "departamento"+i, "provincia"+i, "candidato"+i,
					"partido"+i, i));
		}
		
		System.out.println(msg.toJson());
	}
	
}
