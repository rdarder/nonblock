package com.globant.nonblock.netty.server.message.binding;

public interface JsonBindable {

	String toJson();

	void fromJson(String msg);
	
}
