package com.github.skjolber.mockito.rest.spring;

import java.util.List;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class TomcatMockitoSpringContainerStarter implements ServletContainerInitializer, WebApplicationInitializer {

	private List<Class<?>> mockTargetBeans;
	private List<Class<?>> contextBeans;
	private ApplicationListener<ApplicationContextEvent> listener;

	public TomcatMockitoSpringContainerStarter(List<Class<?>> beans, List<Class<?>> configurationBeans, ApplicationListener<ApplicationContextEvent> listener) {
		this.mockTargetBeans = beans;
		this.contextBeans = configurationBeans;
		this.listener = listener;
	}

	public void onStartup(ServletContext servletContext) throws ServletException {
		// Create the 'root' Spring application context
		
		// Create the dispatcher servlet's Spring application context
		MockitoSpringFactoryWebApplicationContext dispatcherContext = new MockitoSpringFactoryWebApplicationContext(mockTargetBeans);
		
		// web config must be loaded after beans
		for(Class<?> bean : contextBeans) {
			dispatcherContext.register(bean);
		}
		
		dispatcherContext.addApplicationListener(listener);
		
		// Register and map the dispatcher servlet
		ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
		dispatcher.setLoadOnStartup(1);
		dispatcher.addMapping("/");
	}

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext servletContext) throws ServletException {
		onStartup(servletContext);
	}
	
}
