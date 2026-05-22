package com.github.skjolber.mockito.rest.spring;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;

public class UndertowMockitoEndpointServerInstance implements MockitoEndpointServerInstance {

	private static final String SPRING_DISPATCHER_MAPPING_URL = "/*";
	private static final String CONTEXT_PATH = "/";

	protected boolean started = true;
	protected List<Undertow> servers = new ArrayList<>();
	protected List<GracefulShutdownHandler> handlers = new ArrayList<>();

	/**
	 * 
	 * Destroy endpoints.
	 * 
	 */

	public void destroy() throws Exception {
		synchronized (this) {
			started = false;

			for (GracefulShutdownHandler gracefulShutdownHandler : handlers) {
				gracefulShutdownHandler.shutdown();
				gracefulShutdownHandler.awaitShutdown(10000);
			}

			for (Undertow server : servers) {
				server.stop();
			}

			handlers.clear();
			servers.clear();
		}
	}

	/**
	 * 
	 * Stop endpoints.
	 * 
	 */

	public void stop() throws Exception {
		synchronized (this) {
			if(started) {
				started = false;

				for (Undertow server : servers) {
					server.stop();
				}
			}
		}
	}

	/**
	 * 
	 * (Re)start endpoints.
	 * 
	 */

	public void start() throws Exception {
		synchronized (this) {
			if(!started) {
				started = true;
				for (Undertow server : servers) {
					server.start();
				}
			}
		}
	}

	public Map<Class<?>, Object> add(List<Class<?>> mockTargetBeans, List<Class<?>> defaultContextBeans, URL url) throws Exception {
		MockitoSpringApplicationListener configuration = new MockitoSpringApplicationListener();
		configuration.setContextBeans(defaultContextBeans);
		configuration.setMockTargetBeans(mockTargetBeans);

		MockitoSpringWebApplicationContext dispatcherContext = new MockitoSpringWebApplicationContext(mockTargetBeans);

		// web config must be loaded after beans
		for(Class<?> bean : defaultContextBeans) {
			dispatcherContext.register(bean);
		}

		dispatcherContext.addApplicationListener(configuration);

		Undertow undertow = configureUndertow(dispatcherContext, url);
		undertow.start();

		return configuration.getAll();
	}

	private Undertow configureUndertow(MockitoSpringWebApplicationContext context, URL url) throws ServletException {
		// https://github.com/yarosla/spring-undertow/blob/master/src/main/java/ys/undertow/UndertowMain.java
		DeploymentInfo servletBuilder = Servlets.deployment()
				.setClassLoader(Undertow.class.getClassLoader())
				.setContextPath(url.getPath())
				.setDeploymentName("mock")
				.addServlet(createDispatcherServlet(context))
				.addListener(createContextLoaderListener(context));

		DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
		manager.deploy();

		PathHandler path = Handlers.path(Handlers.redirect("/"))
				.addPrefixPath(CONTEXT_PATH, manager.start());

		final GracefulShutdownHandler gracefulHandler = Handlers.gracefulShutdown(exchange -> {
			// Disables persistent connections; forces close after response
			// works around issue where client does not close connection and then crashes on the next request to the same port
			exchange.setPersistent(false); 
			path.handleRequest(exchange);
		});

		Undertow undertow = Undertow.builder()
				.addHttpListener(url.getPort(), url.getHost())
				.setHandler(gracefulHandler)

				.setHandler(gracefulHandler)
				.build();

		handlers.add(gracefulHandler);
		servers.add(undertow);

		return undertow;
	}

	private static ListenerInfo createContextLoaderListener(WebApplicationContext context) {
		InstanceFactory<ContextLoaderListener> factory = new ImmediateInstanceFactory<>(new ContextLoaderListener(context));
		return new ListenerInfo(ContextLoaderListener.class, factory);
	}

	private static ServletInfo createDispatcherServlet(WebApplicationContext context) {
		InstanceFactory<DispatcherServlet> factory = new ImmediateInstanceFactory<>(new DispatcherServlet(context));
		return Servlets.servlet("DispatcherServlet", DispatcherServlet.class, factory)
				.addMapping(SPRING_DISPATCHER_MAPPING_URL)
				.setLoadOnStartup(1);
	}	
}
