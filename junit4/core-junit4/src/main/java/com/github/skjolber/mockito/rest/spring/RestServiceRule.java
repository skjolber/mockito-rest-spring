package com.github.skjolber.mockito.rest.spring;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

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
				throw new IllegalArgumentException();
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
    
    /** beans added to the spring context */
    protected List<Class<?>> defaultContextBeans;
    		
    public RestServiceRule() {
    	this(Arrays.<Class<?>>asList(MockitoEndpointWebMvcConfig.class));
	}
    
    public RestServiceRule(List<Class<?>> contextBeans) {
    	this.defaultContextBeans = contextBeans;
	}

	private List<MockitoEndpointServerInstance> servers = new ArrayList<MockitoEndpointServerInstance>();

	/**
	 * Create (and start) service endpoint with mock delegate. 
	 * 
	 * @param serviceInterface service mock
	 * @param address base address, i.e. http://localhost:1234
	 * @param contextBeans list of context beans
	 * @return map of mocks
	 * @throws Exception if a problem occurred
	 */

    public Map<Class<?>, Object> mock(Class<?> serviceInterface, List<Class<?>> contextBeans, String address) throws Exception {
        // wrap the evaluator mock in proxy
        URL url = new URL(address);
        if (!url.getHost().equals("localhost") && !url.getHost().equals("127.0.0.1")) {
            throw new IllegalArgumentException("Only local mocking is supported");
        }
        
    	ServiceLoader<MockitoEndpointServerInstance> loader = ServiceLoader.load(MockitoEndpointServerInstance.class);
    	Iterator<MockitoEndpointServerInstance> iterator = loader.iterator();
    	if(!iterator.hasNext()) {
    		throw new IllegalArgumentException("Expected implementation of " + MockitoEndpointServerInstance.class.getName() + ", found none");
    	}
    	MockitoEndpointServerInstance server = iterator.next();
        
    	Map<Class<?>, Object> add = server.add(Arrays.asList(serviceInterface), contextBeans, url);

    	servers.add(server);

        return add;
    }
    
    public <T> T mock(Class<T> serviceInterface, String baseAddress) throws Exception {
    	return mock(serviceInterface, defaultContextBeans, baseAddress, null);
    }
    
    public <T> T mock(Class<T> serviceInterface, String baseAddress, String path) throws Exception {
    	return mock(serviceInterface, defaultContextBeans, baseAddress, path);
    }

    @SuppressWarnings("unchecked")
	public <T> T mock(Class<T> serviceInterface, List<Class<?>> contextBeans, String baseAddress, String path) throws Exception {
        MockitoEndpointServiceFactory mockitoEndpointServiceFactory = new MockitoEndpointServiceFactory();

    	if(path != null || serviceInterface.isInterface()) {
    		serviceInterface = mockitoEndpointServiceFactory.asService(serviceInterface, path);
    	}
    	Map<Class<?>, Object> mock = mock(serviceInterface, contextBeans, baseAddress);
    	
        T result = (T) mock.get(serviceInterface);
        if(result == null) {
        	throw new RuntimeException(mock.toString() + " from " + serviceInterface);
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
     * @throws Exception if a problem occurred
     */

    public void destroy() throws Exception {
        for (MockitoEndpointServerInstance endpointImpl : servers) {
            endpointImpl.destroy();
        }
        servers.clear();
    }

    /**
     * 
     * Stop endpoints.
     * @throws Exception if a problem occurred
     */

    public void stop() throws Exception {
        for (MockitoEndpointServerInstance endpointImpl : servers) {
            endpointImpl.stop();
        }
    }

    /**
     * 
     * (Re)start endpoints.
     * @throws Exception if a problem occurred
     */

    public void start() throws Exception {
        for (MockitoEndpointServerInstance endpointImpl : servers) {
            endpointImpl.start();
        }
    }

	public Builder builder(String address) {
		return new Builder(address);
	}
}