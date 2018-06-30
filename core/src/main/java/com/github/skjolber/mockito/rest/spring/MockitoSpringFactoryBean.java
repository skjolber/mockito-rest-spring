package com.github.skjolber.mockito.rest.spring;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

public class MockitoSpringFactoryBean implements FactoryBean<Object> {

	@Autowired 
	protected MockitoSpringWebApplicationContext applicationContext;
	
	protected String className;
	
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
		if(applicationContext == null) {
			return null;
		}
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
