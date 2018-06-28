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

public class JettyMockitoSpringConfiguration extends AbstractConfiguration implements MockitoSpringConfiguration, ApplicationListener<ApplicationContextEvent> {

	protected List<Class<?>> contextBeans;
	protected List<Class<?>> mockTargetBeans;
	protected Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();

	public void setContextBeans(List<Class<?>> contextBeans) {
		this.contextBeans = contextBeans;
	}
	
	public void setMockTargetBeans(List<Class<?>> mockTargetBeans) {
		this.mockTargetBeans = mockTargetBeans;
	}

	@Override
	public void configure(WebAppContext context) throws Exception {
		//add a bean to the context which will call the servletcontainerinitializers when appropriate
		JettyMockitoSpringContainerStarter starter = new JettyMockitoSpringContainerStarter(context, mockTargetBeans, contextBeans, this);
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
