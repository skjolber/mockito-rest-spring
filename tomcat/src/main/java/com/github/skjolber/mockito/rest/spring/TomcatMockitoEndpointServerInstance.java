package com.github.skjolber.mockito.rest.spring;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletRegistration;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.servlet.DispatcherServlet;

import org.apache.catalina.startup.Tomcat.FixContextListener;

public class TomcatMockitoEndpointServerInstance implements MockitoEndpointServerInstance {

	protected List<Tomcat> servers = new ArrayList<>();
    
    /**
     * 
     * Stop endpoints.
     * 
     */

    public void stop() throws Exception {
        for (Tomcat endpointImpl : servers) {
            endpointImpl.stop();
        }
    }

    /**
     * 
     * (Re)start endpoints.
     * 
     */

    public void start() throws Exception {
        for (Tomcat endpointImpl : servers) {
            endpointImpl.start();
        }
    }

	public Map<Class<?>, Object> add(List<Class<?>> mockTargetBeans, List<Class<?>> defaultContextBeans, URL url) throws Exception {
    	TomcatMockitoSpringConfiguration configuration = new TomcatMockitoSpringConfiguration();
    	configuration.setContextBeans(defaultContextBeans);
		configuration.setMockTargetBeans(mockTargetBeans);

    	Tomcat tomcat = new Tomcat();
    	tomcat.setPort(url.getPort());
    	tomcat.setHostname("localhost");
    	
    	// http://www.codejava.net/servers/tomcat/how-to-embed-tomcat-server-into-java-web-applications
    	// https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/web/embedded/tomcat/TomcatServletWebServerFactory.java
    	
		// Create the dispatcher servlet's Spring application context
		MockitoSpringFactoryWebApplicationContext dispatcherContext = new MockitoSpringFactoryWebApplicationContext(mockTargetBeans);
		
		// web config must be loaded after beans
		for(Class<?> bean : defaultContextBeans) {
			dispatcherContext.register(bean);
		}
		
		dispatcherContext.addApplicationListener(configuration);
		
    	String contextPath = "/";
    	String docBase = new File(".").getAbsolutePath();

    	Context context = tomcat.addContext(contextPath, docBase);
    	context.addLifecycleListener(new FixContextListener());
    	
        DispatcherServlet servlet = new DispatcherServlet(dispatcherContext);
        
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default");
		defaultServlet.setLoadOnStartup(1);
		defaultServlet.setOverridable(true);
		defaultServlet.setServlet(servlet);
		
		context.addChild(defaultServlet);
		context.addServletMappingDecoded("/", defaultServlet.getName());        

        tomcat.start();
        
        do {
        	switch(tomcat.getServer().getState()) {
        	case NEW:
        	case INITIALIZING:
        	case INITIALIZED:
        	case STARTING_PREP:
        	case STARTING:
        		Thread.sleep(100);
        		continue;
        	default : break;
        	}
        	break;
        } while(true);
        if(tomcat.getServer().getState() == LifecycleState.STARTED) {
            return configuration.getAll();
        }
        throw new IllegalStateException("Unable to start server");
	}
}
