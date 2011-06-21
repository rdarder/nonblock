package com.globant.nonblock.netty.server.message.binding;

import net.sf.json.JSONObject;

public abstract class Message implements JsonBindable, MessagePart {

	protected String name;
	protected Integer id;
	protected Integer ref;

	protected Message(final String name, final Integer id, final Integer ref) {
		super();
		this.name = name;
		this.id = id;
		this.ref = ref;
	}

	public Message(final String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Integer getId() {
		return id;
	}

	public Integer getRef() {
		return ref;
	}

	@Override
	public void fromJson(final String msg) {

		final JSONObject rootMap = JSONObject.fromObject(msg);
		this.name = (String) rootMap.get("name");
		this.id = (Integer) rootMap.get("id");
		this.ref = (Integer) rootMap.get("ref");

		parseBodyContent(rootMap);
	}

	@Override
	public String toJson() {
		final JSONObject rootMap = new JSONObject();
		rootMap.put("name", this.name);
		rootMap.put("id", this.id);
		rootMap.put("ref", this.ref);

		addBodyContent(rootMap);
		return rootMap.toString();
	}

}
