package com.github.skjolber.mockito.rest.spring;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Rule for mocking remoting endpoints. <br/>
 * <br/>
 * Intended for use when testing remoting clients.
 *
 */

public class RestServiceRule extends org.junit.rules.ExternalResource {

    public static RestServiceRule newInstance() {
        return new RestServiceRule();
    }

    public static RestServiceRule newInstance(List<Class<?>> beans) {
        return new RestServiceRule(beans);
    }
    
    /** beans added to the sprin context */
    private List<Class<?>> contextBeans;
    
    public RestServiceRule() {
    	this(Arrays.<Class<?>>asList(DefaultSpringWebMvcConfig.class));
	}
    
    public RestServiceRule(List<Class<?>> beans) {
    	this.contextBeans = beans;
	}

	private List<Server> servers = new ArrayList<Server>();

    /**
     * Create (and start) service endpoint with mock delegate. 
     * 
     * @param port
     *            service class
     * @param address
     *            address, i.e. http://localhost:1234
     * @return mockito mock - the mock to which server calls are delegated
     * @throws Exception
     */

    public Map<Class<?>, Object> mock(List<Class<?>> serviceInterfaces, String address) throws Exception {
        // wrap the evaluator mock in proxy
        URL url = new URL(address);
        if (!url.getHost().equals("localhost") && !url.getHost().equals("127.0.0.1")) {
            throw new IllegalArgumentException("Only local mocking is supported");
        }
    	WebAppContext webAppContext = new WebAppContext();
    	webAppContext.setContextPath(url.getPath());
    	
    	MockitoSpringConfiguration configuration = new MockitoSpringConfiguration(serviceInterfaces, contextBeans);
    	
    	webAppContext.setConfigurations(new org.eclipse.jetty.webapp.Configuration[] { configuration });
    	webAppContext.setParentLoaderPriority(true);
    	
    	Server server = new Server(url.getPort());
        server.setHandler(webAppContext);

        server.start();
    	
        servers.add(server);
        
        return configuration.getAll();
    }

	
    public <T> T mock(Class<T> serviceInterface, String address) throws Exception {
    	List<Class<?>> mockTargetBeans = new ArrayList<>();
    	mockTargetBeans.add(serviceInterface);

    	Map<Class<?>, Object> mock = mock(mockTargetBeans, address);
    	
        return (T) mock.get(serviceInterface);
    }

    protected void before() throws Throwable {
        super.before();
    }

    protected void after() {
        try {
            destroy();
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 
     * Destroy endpoints.
     * 
     * @throws Exception
     * 
     */

    public void destroy() throws Exception {
        for (Server endpointImpl : servers) {
            endpointImpl.stop();
        }
    }

    /**
     * 
     * Stop endpoints.
     * 
     * @throws Exception
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
     * @throws Exception
     * 
     */

    public void start() throws Exception {
        for (Server endpointImpl : servers) {
            endpointImpl.start();
        }
    }

    

}