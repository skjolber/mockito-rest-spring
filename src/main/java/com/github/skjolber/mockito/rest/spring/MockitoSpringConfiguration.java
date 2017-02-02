package com.github.skjolber.mockito.rest.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

public class MockitoSpringConfiguration extends AbstractConfiguration implements ApplicationListener<ApplicationContextEvent> {

	private List<Class<?>> contextBeans;
	private List<Class<?>> mockTargetBeans;
	private Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();

	public MockitoSpringConfiguration(List<Class<?>> mockTargetBeans, List<Class<?>> contextBeans) {
		this.mockTargetBeans = mockTargetBeans;
		this.contextBeans = contextBeans;
	}

	@Override
	public void configure(WebAppContext context) throws Exception {
		//add a bean to the context which will call the servletcontainerinitializers when appropriate
		MockitoSpringContainerStarter starter = new MockitoSpringContainerStarter(context, mockTargetBeans, contextBeans, this);
        context.addBean(starter, true);
	}
	
	public void onApplicationEvent(ApplicationContextEvent event) {
		if(event instanceof ContextRefreshedEvent) {
			// spring context has been started
			ApplicationContext applicationContext = event.getApplicationContext();
			for(Class<?> bean : mockTargetBeans) {
				Object value = applicationContext.getBean(bean);
				
				map.put(bean, value);
			}
		}
	}

	public Object get(Class<?> cls) {
		return map.get(cls);
	}

	public Map<Class<?>, Object> getAll() {
		return map;
	}
	
}
