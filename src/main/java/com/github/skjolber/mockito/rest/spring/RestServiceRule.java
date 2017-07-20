package com.github.skjolber.mockito.rest.spring;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType.Loaded;

/**
 * Rule for mocking remoting endpoints. <br>
 * <br>
 * Intended for use when testing service clients.
 *
 */

public class RestServiceRule extends org.junit.rules.ExternalResource {

    public static <T> Class<T> asService(Class<T> serviceInterface) throws Exception {
    	return asService(serviceInterface, null);
    }

    public static <T> Class<T> asService(Class<T> serviceInterface, String path) throws Exception {
    	
    	net.bytebuddy.dynamic.DynamicType.Builder<T> subclass = new ByteBuddy().subclass(serviceInterface);
    	
    	if(!serviceInterface.isAnnotationPresent(RestController.class) && !serviceInterface.isAnnotationPresent(Controller.class)) {
    		subclass = subclass.annotateType(AnnotationDescription.Builder.ofType(RestController.class).build());
    	}
    	if(path != null) {
	   		subclass = subclass.annotateType(AnnotationDescription.Builder.ofType(org.springframework.web.bind.annotation.RequestMapping.class).defineArray("path", new String[]{path}).build());
    	}
    	
   		Loaded<T> load = subclass.make().load(serviceInterface.getClassLoader());

   		return (Class<T>) load.getLoaded();
    }

	public class Builder {
		
		public Builder(String address) {
			this.address = address;
		}

		private String address;
		
		private List<Class<?>> beans = new ArrayList<>();
		
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
			if(path != null || serviceInterface.isInterface()) {
				beans.add(RestServiceRule.asService(serviceInterface, path));
			} else {
				beans.add(serviceInterface);
			}
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
    private List<Class<?>> defaultContextBeans;
    
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
    	
    	MockitoSpringConfiguration configuration = new MockitoSpringConfiguration(serviceInterfaces, contextBeans);
    	
    	webAppContext.setConfigurations(new org.eclipse.jetty.webapp.Configuration[] { configuration });
    	webAppContext.setParentLoaderPriority(true);
    	
    	Server server = new Server(url.getPort());
        server.setHandler(webAppContext);

        servers.add(server);

       	server.start();

        return configuration.getAll();
    }
    
    public <T> T mock(Class<T> serviceInterface, String baseAddress) throws Exception {
    	return mock(serviceInterface, defaultContextBeans, baseAddress, null);
    }
    
    public <T> T mock(Class<T> serviceInterface, String baseAddress, String path) throws Exception {
    	return mock(serviceInterface, defaultContextBeans, baseAddress, path);
    }

    public <T> T mock(Class<T> serviceInterface, List<Class<?>> contextBeans, String baseAddress, String path) throws Exception {
    	if(path != null || serviceInterface.isInterface()) {
    		serviceInterface = asService(serviceInterface, path);
    	}
     	List<Class<?>> mockTargetBeans = new ArrayList<>();
    	mockTargetBeans.add(serviceInterface);

    	Map<Class<?>, Object> mock = mock(mockTargetBeans, contextBeans, baseAddress);
    	
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