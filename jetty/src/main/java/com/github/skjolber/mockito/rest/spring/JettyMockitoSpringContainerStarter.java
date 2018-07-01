package com.github.skjolber.mockito.rest.spring;

import java.util.List;

import javax.servlet.ServletRegistration;

import org.eclipse.jetty.server.handler.ContextHandler.Context;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

public class JettyMockitoSpringContainerStarter extends AbstractLifeCycle implements ServletContextHandler.ServletContainerInitializerCaller {

	private WebAppContext context;
	private List<Class<?>> mockTargetBeans;
	private List<Class<?>> contextBeans;
	private ApplicationListener<ApplicationContextEvent> listener;

	public JettyMockitoSpringContainerStarter(WebAppContext context, List<Class<?>> beans, List<Class<?>> configurationBeans, ApplicationListener<ApplicationContextEvent> listener) {
		this.context = context;
		this.mockTargetBeans = beans;
		this.contextBeans = configurationBeans;
		this.listener = listener;
	}

	public void doStart() {
		Context container = context.getServletContext();
		container.setExtendedListenerTypes(true);

		// Create the 'root' Spring application context
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
		// Manage the lifecycle of the root application context

		ContextLoaderListener contextLoaderListener = new ContextLoaderListener(rootContext);
		container.addListener(contextLoaderListener);

		rootContext.setClassLoader(context.getClassLoader());

		// Create the dispatcher servlet's Spring application context
		MockitoSpringWebApplicationContext dispatcherContext = new MockitoSpringWebApplicationContext(mockTargetBeans);
		dispatcherContext.setClassLoader(context.getClassLoader());

		// web config must be loaded after beans
		for(Class<?> bean : contextBeans) {
			dispatcherContext.register(bean);
		}

		dispatcherContext.register(WebMvcConfigurationSupport.class);

		dispatcherContext.addApplicationListener(listener);

		// Register and map the dispatcher servlet
		ServletRegistration.Dynamic dispatcher = container.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
		dispatcher.setLoadOnStartup(1);
		dispatcher.addMapping("/");
	}

}
