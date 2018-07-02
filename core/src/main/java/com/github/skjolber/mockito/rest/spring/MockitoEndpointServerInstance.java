package com.github.skjolber.mockito.rest.spring;

import java.net.URL;
import java.util.List;
import java.util.Map;

public interface MockitoEndpointServerInstance {

	void destroy() throws Exception;

	void start() throws Exception;
	
	void stop() throws Exception;

	Map<Class<?>, Object> add(List<Class<?>> mockTargetBeans, List<Class<?>> defaultContextBeans, URL address) throws Exception;
}
