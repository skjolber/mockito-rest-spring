package com.github.skjolber.mockito.rest.spring;
import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class MockitoSpringFactoryBean implements FactoryBean<Object> {

	@Autowired 
	private MockitoSpringFactoryWebApplicationContext applicationContext;
	
	private String className;
	
	@Override
	public Object getObject() throws Exception {
		Class<?> objectType = getObjectType();
		if(objectType == null) {
			throw new RuntimeException();
		}
		return org.mockito.Mockito.mock(objectType);
	}

	@Override
	public Class<?> getObjectType() {
		Class<?> result = applicationContext.findClass(className);
		if(result != null) {
			return result;
		}
		try {
			return Class.forName(className, false, applicationContext.getClassLoader());
		} catch (ClassNotFoundException e) {
			return null;
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
