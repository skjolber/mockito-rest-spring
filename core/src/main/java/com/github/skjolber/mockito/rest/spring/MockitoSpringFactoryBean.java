package com.github.skjolber.mockito.rest.spring;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

public class MockitoSpringFactoryBean implements FactoryBean<Object> {

	@Autowired 
	protected MockitoSpringWebApplicationContext applicationContext;
	
	protected String className;
	
	@Override
	public Object getObject() throws Exception {
		return applicationContext.getMock(className);
	}

	@Override
	public Class<?> getObjectType() {
		if(applicationContext == null) {
			return null;
		}
		return applicationContext.findClass(className);
	}

	@Override
	public boolean isSingleton() {
		return true; // if false, new beans are created all the time
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
}
