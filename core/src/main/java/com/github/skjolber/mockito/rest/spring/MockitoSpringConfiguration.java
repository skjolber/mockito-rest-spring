package com.github.skjolber.mockito.rest.spring;

import java.util.List;

public interface MockitoSpringConfiguration {

	void setContextBeans(List<Class<?>> contextBeans);
	
	void setMockTargetBeans(List<Class<?>> mockTargetBeans);
}
