package com.github.skjolber.mockito.rest.spring;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyMockitoEndpointServerInstance implements MockitoEndpointServerInstance {

	protected List<Server> servers = new ArrayList<Server>();
    
    /**
     * 
     * Stop endpoints.
     * 
     */

    public void stop() throws Exception {
        for (Server endpointImpl : servers) {
            endpointImpl.stop();
        }
    }

    /**
     * 
     * (Re)start endpoints.
     * 
     */

    public void start() throws Exception {
        for (Server endpointImpl : servers) {
            endpointImpl.start();
        }
    }

	public Map<Class<?>, Object> add(List<Class<?>> mockTargetBeans, List<Class<?>> defaultContextBeans, URL url) throws Exception {
    	WebAppContext webAppContext = new WebAppContext();
    	webAppContext.setContextPath(url.getPath());

    	MockitoSpringConfiguration mockitoSpringConfiguration = new MockitoSpringConfiguration(); 
    	mockitoSpringConfiguration.setContextBeans(defaultContextBeans);
    	mockitoSpringConfiguration.setMockTargetBeans(mockTargetBeans);

    	JettyMockitoSpringConfiguration configuration = new JettyMockitoSpringConfiguration(mockitoSpringConfiguration);
    	
    	webAppContext.setConfigurations(new org.eclipse.jetty.webapp.Configuration[] { configuration });
    	webAppContext.setParentLoaderPriority(true);
    	
    	Server server = new Server(url.getPort());
        server.setHandler(webAppContext);

        servers.add(server);

       	server.start();

        return mockitoSpringConfiguration.getAll();
	}
	
}
