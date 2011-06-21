package com.globant.nonblock.netty.server.message.newdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.globant.nonblock.netty.server.message.binding.Message;

public class NewDataMessage extends Message {

	public NewDataMessage(Integer id, Integer ref) {
		super("newdata", id, ref);
	}

	private List<NewDataDatum> datos = new ArrayList<NewDataDatum>();

	public List<NewDataDatum> getDatos() {
		return datos;
	}

	public void setDatos(final List<NewDataDatum> datos) {
		this.datos = datos;
	}

	public void addBodyContent(final JSONObject msg) {
		JSONArray datum = new JSONArray();
		for (NewDataDatum d : this.datos) {
			JSONObject datumMap = JSONObject.fromObject(new HashMap<String, Object>());
			datumMap.put("puesto", d.getPuesto());
			datumMap.put("mesa", d.getMesa());
			datumMap.put("local", d.getLocal());
			datumMap.put("seccional", d.getSeccional());
			datumMap.put("localidad", d.getLocalidad());
			datumMap.put("departamento", d.getDepartamento());
			datumMap.put("provincia", d.getProvincia());
			datumMap.put("candidato", d.getCandidato());
			datumMap.put("partido", d.getPartido());
			datumMap.put("votos", d.getVotos());
			datum.add(datumMap);
		}
		msg.put("data", datum);
	}

	public void parseBodyContent(JSONObject data) {
	}

}
