package com.github.skjolber.mockito.rest.spring;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

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
import org.springframework.core.io.support.SpringFactoriesLoader;

import com.github.skjolber.mockito.rest.spring.api.MockEndpoint;
import com.github.skjolber.mockito.rest.spring.mockito.MockEndpointFieldHelper;

public class MockitoEndpointExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor,
BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback,
ParameterResolver {

    private static final String PORT_NAME = "mockitoRestSpringServerPort";
    
    public static int getPort() {
    	String property = System.getProperty(PORT_NAME);
    	if(property == null) {
    		throw new IllegalArgumentException("Port not set");
    	}
    	return Integer.parseInt(property);
    }
    
	private PortReservations portReservations;
    private MockitoEndpointServiceFactory serviceFactory = new MockitoEndpointServiceFactory();
    
    /** beans added to the Spring context */
    private List<Class<?>> defaultContextBeans;
	private MockitoEndpointServerInstance server;
    
    public MockitoEndpointExtension() {
    	this(Arrays.<Class<?>>asList(DefaultSpringWebMvcConfig.class));
    	
    	ServiceLoader<MockitoEndpointServerInstance> loader = ServiceLoader.load(MockitoEndpointServerInstance.class);
    	Iterator<MockitoEndpointServerInstance> iterator = loader.iterator();
    	if(!iterator.hasNext()) {
    		throw new IllegalArgumentException("Expected implementation of " + MockitoEndpointServerInstance.class.getName() + ", found none");
    	}
    	server = iterator.next();
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
		
		Map<Class<?>, Field> fields = new HashMap<>();
		for (Field field : helper.getFields()) {
			
			MockEndpoint annotation = field.getAnnotation(MockEndpoint.class);
			if(annotation != null) {
				
				String path = annotation.path();
				if(path.isEmpty()) {
					path = null;
				}
				
				Class<?> service = serviceFactory.add(field.getType(), path);
				fields.put(service, field);
			}
		}
		
		String address = String.format("http://localhost:%s", portReservations.getPorts().get(PORT_NAME));
		
		Map<Class<?>, Object> mocks = mock(address);
		
		for (Entry<Class<?>, Field> entry : fields.entrySet()) {
			helper.setField(entry.getValue(), mocks.get(entry.getKey()));
		}
		
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		portReservations.stop();
		
		server.stop();
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
	    portReservations = new PortReservations(PORT_NAME);
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

    public Map<Class<?>, Object> mock(String address) throws Exception {
        // wrap the evaluator mock in proxy
        URL url = new URL(address);
        if (!url.getHost().equals("localhost") && !url.getHost().equals("127.0.0.1")) {
            throw new IllegalArgumentException("Only local mocking is supported");
        }
        
    	List<Class<?>> mockTargetBeans = serviceFactory.getBeans();

    	return server.add(mockTargetBeans, defaultContextBeans, url);
    }

    /**
     * 
     * Stop endpoints.
     * 
     */

    public void stop() throws Exception {
       	server.stop();
    }

    /**
     * 
     * (Re)start endpoints.
     * 
     */

    public void start() throws Exception {
    	server.start();
    }
	
}
