package com.github.skjolber.mockito.rest.spring;
import org.springframework.beans.factory.FactoryBean;

public class MockitoSpringFactoryBean implements FactoryBean {

	private String className;
	
	@Override
	public Object getObject() throws Exception {
		return org.mockito.Mockito.mock(getObjectType());
	}

	@Override
	public Class getObjectType() {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public boolean isSingleton() {
		return true; // if false, new beans are created all the time
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
}
