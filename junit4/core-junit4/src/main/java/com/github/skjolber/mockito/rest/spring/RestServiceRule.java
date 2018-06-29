package com.github.skjolber.mockito.rest.spring;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Rule for mocking remoting endpoints. <br>
 * <br>
 * Intended for use when testing service clients.
 *
 */

public class RestServiceRule extends org.junit.rules.ExternalResource {

	public class Builder extends MockitoEndpointServiceFactory {
		
		public Builder(String address) {
			this.address = address;
		}

		private String address;
		
		private List<Class<?>> contextBeans = new ArrayList<>(RestServiceRule.this.defaultContextBeans);
		
		public <T> T mock() throws Exception {
			if(beans.isEmpty()) {
				throw new IllegalArgumentException("No beans added");
			}
			
			if(beans.size() == 1) {
				return (T) RestServiceRule.this.mock(beans.get(0), address);
			} else {
				return (T) RestServiceRule.this.mock(beans, address);
			}
		}
		
		public Builder service(Class<?> serviceClass) throws Exception {
			return service(serviceClass, null);
		}

		public Builder service(Class<?> serviceInterface, String path) throws Exception {
			add(serviceInterface, path);
			return this;
		}

		public Builder context(Class<?> context) {
			contextBeans.add(context);
			
			return this;
		}

	}
	
    public static RestServiceRule newInstance() {
        return new RestServiceRule();
    }

	public static RestServiceRule newInstance(List<Class<?>> beans) {
        return new RestServiceRule(beans);
    }
    
    /** beans added to the sprin context */
    protected List<Class<?>> defaultContextBeans;
    protected MockitoEndpointServiceFactory mockitoEndpointServiceFactory = new MockitoEndpointServiceFactory();
    		
    public RestServiceRule() {
    	this(Arrays.<Class<?>>asList(DefaultSpringWebMvcConfig.class));
	}
    
    public RestServiceRule(List<Class<?>> contextBeans) {
    	this.defaultContextBeans = contextBeans;
	}

	private List<Server> servers = new ArrayList<Server>();

	/**
	 * Create (and start) service endpoint with mock delegates. 
	 * 
	 * @param serviceInterfaces a list of desired service mocks
	 * @param address base address, i.e. http://localhost:1234
	 * @return map of mocks
	 * @throws Exception if a problem occurred
	 */
    public Map<Class<?>, Object> mock(List<Class<?>> serviceInterfaces, String address) throws Exception {
    	return mock(serviceInterfaces, defaultContextBeans, address);
    }

    public Map<Class<?>, Object> mock(List<Class<?>> serviceInterfaces, List<Class<?>> contextBeans, String address) throws Exception {
        // wrap the evaluator mock in proxy
        URL url = new URL(address);
        if (!url.getHost().equals("localhost") && !url.getHost().equals("127.0.0.1")) {
            throw new IllegalArgumentException("Only local mocking is supported");
        }
    	WebAppContext webAppContext = new WebAppContext();
    	webAppContext.setContextPath(url.getPath());
    	
    	MockitoSpringConfiguration mockitoSpringConfiguration = new MockitoSpringConfiguration(); 
    	mockitoSpringConfiguration.setContextBeans(defaultContextBeans);
    	mockitoSpringConfiguration.setMockTargetBeans(serviceInterfaces);

    	JettyMockitoSpringConfiguration configuration = new JettyMockitoSpringConfiguration(mockitoSpringConfiguration);
    	
    	webAppContext.setConfigurations(new org.eclipse.jetty.webapp.Configuration[] { configuration });
    	webAppContext.setParentLoaderPriority(false);
    	
    	webAppContext.setClassLoader(mockitoEndpointServiceFactory.getClassLoader());
    	
    	Server server = new Server(url.getPort());
        server.setHandler(webAppContext);

        servers.add(server);

       	server.start();

        return mockitoSpringConfiguration.getAll();
    }
    
    public <T> T mock(Class<T> serviceInterface, String baseAddress) throws Exception {
    	return mock(serviceInterface, defaultContextBeans, baseAddress, null);
    }
    
    public <T> T mock(Class<T> serviceInterface, String baseAddress, String path) throws Exception {
    	return mock(serviceInterface, defaultContextBeans, baseAddress, path);
    }

    public <T> T mock(Class<T> serviceInterface, List<Class<?>> contextBeans, String baseAddress, String path) throws Exception {
    	if(path != null || serviceInterface.isInterface()) {
    		serviceInterface = mockitoEndpointServiceFactory.asService(serviceInterface, path);
    	}
     	List<Class<?>> mockTargetBeans = new ArrayList<>();
    	mockTargetBeans.add(serviceInterface);

    	Map<Class<?>, Object> mock = mock(mockTargetBeans, contextBeans, baseAddress);
    	
        T result = (T) mock.get(serviceInterface);
        if(result == null) {
        	throw new RuntimeException(mock.toString() + " from " + mockTargetBeans);
        }
        return result;
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

	public Builder builder(String address) {
		return new Builder(address);
	}

    

}