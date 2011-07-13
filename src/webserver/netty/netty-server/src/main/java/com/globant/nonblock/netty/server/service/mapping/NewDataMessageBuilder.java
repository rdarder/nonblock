package com.globant.nonblock.netty.server.service.mapping;

import java.util.List;

import com.globant.nonblock.netty.server.message.newdata.NewDataDatum;
import com.globant.nonblock.netty.server.message.newdata.NewDataMessage;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;

public class NewDataMessageBuilder {

	public static NewDataMessage buildFromQuery(final List<Object[]> result, final SubscribeMessage subscribeMessage) {
		
		final NewDataMessage resultMessage = new NewDataMessage(null, subscribeMessage.getId());
		
		for (Object[] e : result) {
			Long cant = (Long) e[0];
			String puesto = (String) e[1];
			String candidato = (String) e[2];
			String partido = (String) e[3];
			String mesa = null, local = null, seccional = null, localidad = null, departamento = null, provincia = null;
			int i = 4;
			switch (subscribeMessage.getNivel()) {
			case Mesa:
				mesa = (String) e[i++];
			case Local:
				local= (String) e[i++];
			case Seccional:
				seccional = (String) e[i++];
			case Localidad:
				localidad = (String) e[i++];
			case Departamento:
				departamento = (String) e[i++];
			case Provincia:
				provincia = (String) e[i++];
			}
			resultMessage.getDatos().add(new NewDataDatum(puesto, mesa, local, seccional, localidad,
					departamento, provincia, candidato, partido, cant.intValue()));
		}
		return resultMessage;
	}

	
}
