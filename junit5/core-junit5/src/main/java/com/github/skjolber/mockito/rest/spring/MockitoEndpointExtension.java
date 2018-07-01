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
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.mockito.Mockito;

import com.github.skjolber.mockito.rest.spring.api.MockEndpoint;
import com.github.skjolber.mockito.rest.spring.mockito.MockEndpointFieldHelper;

public class MockitoEndpointExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor,
BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final String PORT_NAME = "mockitoRestSpringServerPort";
    
    public static int getPort() {
    	String property = System.getProperty(PORT_NAME);
    	if(property == null) {
    		throw new IllegalArgumentException("Port not set");
    	}
    	return Integer.parseInt(property);
    }
    
	protected MockitoEndpointServiceFactory serviceFactory = new MockitoEndpointServiceFactory();
    
    /** beans added to the Spring context */
    protected List<Class<?>> defaultContextBeans;
    protected MockitoEndpointServerInstance server;
	
	protected boolean postProcessed = false;
	protected Map<Field, Object> setters;

	protected PortReservations portReservations;
    
    public MockitoEndpointExtension() {
    	this(Arrays.<Class<?>>asList(MockitoEndpointWebMvcConfig.class));
    	
    	ServiceLoader<MockitoEndpointServerInstance> loader = ServiceLoader.load(MockitoEndpointServerInstance.class);
    	Iterator<MockitoEndpointServerInstance> iterator = loader.iterator();
    	if(!iterator.hasNext()) {
    		throw new IllegalArgumentException("Expected implementation of " + MockitoEndpointServerInstance.class.getName() + ", found none");
    	}
    	server = iterator.next();
	}
    
    public MockitoEndpointExtension(MockitoEndpointServerInstance server) {
    	this(Arrays.<Class<?>>asList(MockitoEndpointWebMvcConfig.class));
    	
    	this.server = server;
	}    
    
    public MockitoEndpointExtension(List<Class<?>> contextBeans) {
    	this.defaultContextBeans = contextBeans;
	}

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		for (Entry<Field, Object> entry : setters.entrySet()) {
			Mockito.reset(entry.getValue());
		}
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		for (Entry<Field, Object> entry : setters.entrySet()) {
			Mockito.reset(entry.getValue());
		}		
	}

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
		MockEndpointFieldHelper helper = new MockEndpointFieldHelper(testInstance, testInstance.getClass());

		if(!postProcessed) {
			postProcessed = true;
						
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
			
			portReservations.release();
			
			Map<Class<?>, Object> mocksByClass = mock(address);
			
			Map<Field, Object> setters = new HashMap<>();
			
			for (Entry<Class<?>, Field> entry : fields.entrySet()) {
				Object mock = mocksByClass.get(entry.getKey());
				
				setters.put(entry.getValue(), mock);
			}
			
			this.setters = setters;
		}

		for (Entry<Field, Object> entry : setters.entrySet()) {
			helper.setField(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		// ideally clean up system property 'mockitoRestSpringServerPort' here, 
		// but then we would always have to also reload the spring context
		// for each new server (unit test class)
		portReservations.release();
		
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
     * @throws Exception if a problem occurred
     */

    public void stop() throws Exception {
       	server.stop();
    }

    /**
     * 
     * (Re)start endpoints.
     * @throws Exception if a problem occurred
     */

    public void start() throws Exception {
    	server.start();
    }
	
}
