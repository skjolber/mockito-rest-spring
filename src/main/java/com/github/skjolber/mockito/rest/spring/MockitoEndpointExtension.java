package com.github.skjolber.mockito.rest.spring;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import com.github.skjolber.mockito.rest.spring.api.MockEndpoint;
import com.github.skjolber.mockito.rest.spring.mockito.MockEndpointFieldHelper;

public class MockitoEndpointExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor,
BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback,
ParameterResolver {

    private PortReservations portReservations;
    private MockitoEndpointServiceFactory serviceFactory = new MockitoEndpointServiceFactory();
    
    /** beans added to the sprin context */
    private List<Class<?>> defaultContextBeans;
	private List<Server> servers = new ArrayList<Server>();
    
    public MockitoEndpointExtension() {
    	this(Arrays.<Class<?>>asList(DefaultSpringWebMvcConfig.class));
	}
    
    public MockitoEndpointExtension(List<Class<?>> contextBeans) {
    	this.defaultContextBeans = contextBeans;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return false;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		throw new RuntimeException();
	}

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
		
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		
	}

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
		MockEndpointFieldHelper helper = new MockEndpointFieldHelper(testInstance, testInstance.getClass());
		
		List<Class<?>> serviceInterfaces = new ArrayList<>();
		
		Map<Class<?>, Field> fields = new HashMap<>();
		for (Field field : helper.getFields()) {
			
			if(field.isAnnotationPresent(MockEndpoint.class)) {
				
				Class<?> service = serviceFactory.service(field.getType());
				
				serviceInterfaces.add(service);
				
				fields.put(service, field);
			}
		}
		
		String address = String.format("http://localhost:%s", portReservations.getPorts().get("mockitoSpringEndpointPort"));
		
		Map<Class<?>, Object> mocks = mock(serviceInterfaces, address);
		
		for (Entry<Class<?>, Field> entry : fields.entrySet()) {
			Object object = mocks.get(entry.getKey());
			System.out.println("Mock is " + object);
			
			helper.setField(entry.getValue(), mocks.get(entry.getKey()));
		}
		
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		portReservations.stop();
		
        for (Server endpointImpl : servers) {
            endpointImpl.stop();
        }
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
	    portReservations = new PortReservations("mockitoSpringEndpointPort");
		portReservations.start();
	}
	
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
	
}
