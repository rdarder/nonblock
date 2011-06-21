package com.globant.nonblock.netty.server.service.mapping;

import java.util.List;

import com.globant.nonblock.netty.server.message.newdata.NewDataDatum;
import com.globant.nonblock.netty.server.message.newdata.NewDataMessage;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;

public class NewDataMessageBuilder {

	public static NewDataMessage buildFromQuery(List<Object[]> result, SubscribeMessage subscribeMessage) {
		NewDataMessage resultMessage = new NewDataMessage(null, subscribeMessage.getId());
		for (Object[] e : result) {
			Long cant = (Long) e[0];
			String nivel = (String) e[1];
			String candidato = (String) e[2];
			resultMessage.getDatos().add(new NewDataDatum("", "", "", "", "", "", "", candidato, "", Integer.valueOf(cant.intValue())));
		}
		return resultMessage;
	}

	
}
