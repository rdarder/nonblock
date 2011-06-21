package com.globant.nonblock.netty.server.message.loader;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.globant.nonblock.netty.server.message.binding.Message;

public class SubmitVotesMessage extends Message {

	public SubmitVotesMessage() {
		super("submitVotes");
	}

	public SubmitVotesMessage(final Integer id, final Integer ref) {
		super("submiteVotes", id, ref);
	}

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

	public void addBodyContent(JSONObject data) {
	}

	public void parseBodyContent(JSONObject data) {
		JSONObject jo = data.getJSONObject("data");
		this.mesa = (String) jo.get("mesa");
		JSONArray votos = (JSONArray) jo.get("votos");
		for (Object v : votos) {
			VoteDatum vd = new VoteDatum();
			vd.addBodyContent((JSONObject) v);
			this.data.add(vd);
		}

	}

}
