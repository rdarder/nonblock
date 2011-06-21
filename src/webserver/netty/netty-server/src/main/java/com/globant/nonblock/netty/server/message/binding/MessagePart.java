package com.globant.nonblock.netty.server.message.binding;

import net.sf.json.JSONObject;

public interface MessagePart {

	void parseBodyContent(JSONObject data);

	void addBodyContent(JSONObject data);

}
