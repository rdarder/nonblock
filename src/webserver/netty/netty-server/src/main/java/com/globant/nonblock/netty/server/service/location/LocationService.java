package com.globant.nonblock.netty.server.service.location;

import java.util.Set;

import com.globant.nonblock.netty.server.lifecycle.LifecycleComponent;

public interface LocationService extends LifecycleComponent {

	Location findLocation(String mesa);

	Set<String> getAllMesas();

}
