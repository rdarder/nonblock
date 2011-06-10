package com.globant.nonblock.netty.server.message.newdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.globant.nonblock.netty.server.message.ServerMessage;

public class NewDataMessage implements ServerMessage {

	List<NewDataDatum> datos = new ArrayList<NewDataDatum>();
	
	public List<NewDataDatum> getDatos() {
		return datos;
	}

	public void setDatos(List<NewDataDatum> datos) {
		this.datos = datos;
	}

	@Override
	public String toJson() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("event", "newData");
		
		List<Map<String, Object>> datum = new ArrayList<Map<String,Object>>();
		map.put("data", datum);
		for (NewDataDatum d : this.datos) {		
			Map<String, Object> datumMap = new HashMap<String, Object>();
			datumMap.put("nivel", d.getNivel());
			datumMap.put("candidato", d.getCandidato());
			datumMap.put("cant", d.getCant());
			datum.add(datumMap);
		}
		return JSONObject.fromObject(map).toString();
	}

	public static void main(String[] args) {
	
		 NewDataMessage m = new NewDataMessage();
		 
		 for (int i = 0; i< 1; i++) {
			 NewDataDatum ndd = new NewDataDatum(Long.valueOf(i), "Provincia" + i, "Pedrito");
			 m.getDatos().add(ndd);
		 }
	
		 System.out.println(m.toJson());
	}
	
}
