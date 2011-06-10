package com.globant.nonblock.netty.server.message.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.globant.nonblock.netty.server.message.ClientMessage;

public class VotesResult implements ClientMessage {

	private String mesa;

	private List<VoteDatum> data = new ArrayList<VoteDatum>();

	public String getMesa() {
		return mesa;
	}

	public void setMesa(String mesa) {
		this.mesa = mesa;
	}

	public List<VoteDatum> getData() {
		return data;
	}

	public void setData(List<VoteDatum> data) {
		this.data = data;
	}

	@Override
	public void fromString(final String json) {
		Map<String, Object> jo = JSONObject.fromObject(json);
		this.mesa = (String) jo.get("mesa");
		JSONArray votos = (JSONArray) jo.get("votos");
		
		for (Object v : votos) {
			VoteDatum vd = new VoteDatum();
			vd.fromString(v.toString());
			this.data.add(vd);
		}
	}

}
