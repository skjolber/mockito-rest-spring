package com.github.skjolber.mockito.rest.spring;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;

import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.DeploymentManager.State;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;

public class UndertowMockitoEndpointServerInstance implements MockitoEndpointServerInstance {

	private static final String SPRING_DISPATCHER_MAPPING_URL = "/*";
	private static final String CONTEXT_PATH = "/";

	protected boolean started = true;
	protected List<UndertowServers> servers = new ArrayList<>();

	protected static class UndertowServers {
		Undertow server;
		SocketAddress endpoint;
	}
	
	/**
	 * 
	 * Destroy endpoints.
	 * 
	 */

	public void destroy() throws Exception {
		synchronized (this) {
			started = false;

			for (UndertowServers server : servers) {
				server.server.stop();
			}

			// wait untill ports free
			for (UndertowServers server : servers) {
				ServerSocket socket = new ServerSocket();
				while(true) {
					try {
						socket.bind(server.endpoint);
						socket.close();
						break;
					} catch(Exception e) {
						Thread.sleep(10);
						e.printStackTrace();
					}
				}
			}

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
	
				for (UndertowServers server : servers) {
					server.server.stop();
				}

				// wait untill ports free
				for (UndertowServers server : servers) {
					ServerSocket socket = new ServerSocket();
					while(true) {
						try {
							socket.bind(server.endpoint);
							socket.close();
							break;
						} catch(Exception e) {
							Thread.sleep(10);
							e.printStackTrace();
						}
					}
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
				for (UndertowServers server : servers) {
					server.server.start();
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

		SimpleSpringApplicationListener simpleSpringApplicationListener = new SimpleSpringApplicationListener();
		dispatcherContext.addApplicationListener(simpleSpringApplicationListener);

		// https://github.com/yarosla/spring-undertow/blob/master/src/main/java/ys/undertow/UndertowMain.java
		DeploymentInfo servletBuilder = Servlets.deployment()
				.setClassLoader(Undertow.class.getClassLoader())
				.setContextPath(url.getPath())
				.setDeploymentName("mock")
				.addServlet(createDispatcherServlet(dispatcherContext))
				.addListener(createContextLoaderListener(dispatcherContext));

		DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
		manager.deploy();

		PathHandler path = Handlers.path(Handlers.redirect("/"))
				.addPrefixPath(CONTEXT_PATH, manager.start());

		Undertow undertow = Undertow.builder()
				.addHttpListener(url.getPort(), url.getHost())
				.setHandler(path)
				.build();		

		UndertowServers s = new UndertowServers();
		s.server = undertow;
		
		servers.add(s);
		
		undertow.start();

		s.endpoint = undertow.getListenerInfo().get(0).getAddress();

		

		return configuration.getAll();
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
