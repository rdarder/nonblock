package com.globant.nonblock.netty.server.message;

import java.util.Map;

import com.globant.nonblock.netty.server.message.loader.VotesResult;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;

import net.sf.json.JSONObject;

public class MessageParser {

	@SuppressWarnings("unchecked")
	public ClientMessage parseClientMessage(String jsonMesage) {

		final Map<String, Object> rootMap = JSONObject.fromObject(jsonMesage);
		final String eventName = (String) rootMap.get("event");
		final Map<String, Object> data = (Map<String, Object>) rootMap.get("data");

		if (eventName.equals("subscribe")) {
			SubscribeMessage message = new SubscribeMessage();
			message.fromString(data.toString());
			return message;
		} else if (eventName.equals("newResult")) {
			VotesResult message = new VotesResult();
			message.fromString(data.toString());
			return message;
		}
		return null;

	}
	
	public static void main(String[] args) {
		String msg = "{ event : 'subscribe', " +
				"data : { puesto : 'Intendente', " + 
				"alcance : 'Provincia', " +
				"valor_alcance : 'Santa Fe'," +
				"nivel : 'Localidad'}}";

		String msg2 = "{ event: 'newResult', " +
				"        data: { mesa: 'Mesa 1', " +
				"                votos: [ { " +
				"			               puesto: 'Intendente'," +
				"			               candidato: 'Julian'," +
				"			               partido: 'PSD' ," +
				"			               cant: 19 } ]" +
				"              } } ";
		new MessageParser().parseClientMessage(msg2);
	}
}
