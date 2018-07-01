package com.github.skjolber.mockito.rest.spring;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.Tomcat.FixContextListener;
import org.springframework.web.servlet.DispatcherServlet;

public class TomcatMockitoEndpointServerInstance implements MockitoEndpointServerInstance {

	protected boolean started = true;
	protected List<Tomcat> servers = new ArrayList<>();
    
	   /**
     * 
     * Stop endpoints.
     * 
     */

    public void stop() throws Exception {
    	if(started) {
    		started = false;
    		
	        for (Tomcat server : servers) {
	        	stop(server);
	        }
    	}
    }
    	
    /**
     * 
     * (Re)start endpoints.
     * 
     */

    public void start() throws Exception {
    	if(!started) {
    		started = true;
	        for (Tomcat server : servers) {
	        	start(server);
	        }
    	}
    }

	public Map<Class<?>, Object> add(List<Class<?>> mockTargetBeans, List<Class<?>> defaultContextBeans, URL url) throws Exception {
		MockitoSpringApplicationListener configuration = new MockitoSpringApplicationListener();
    	configuration.setContextBeans(defaultContextBeans);
		configuration.setMockTargetBeans(mockTargetBeans);

    	Tomcat tomcat = new Tomcat();
    	tomcat.setPort(url.getPort());
    	tomcat.setHostname("localhost");
    	
    	// http://www.codejava.net/servers/tomcat/how-to-embed-tomcat-server-into-java-web-applications
    	// https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/web/embedded/tomcat/TomcatServletWebServerFactory.java
    	
		// Create the dispatcher servlet's Spring application context
    	MockitoSpringWebApplicationContext dispatcherContext = new MockitoSpringWebApplicationContext(mockTargetBeans);
		
		// web config must be loaded after beans
		for(Class<?> bean : defaultContextBeans) {
			dispatcherContext.register(bean);
		}
		
		dispatcherContext.addApplicationListener(configuration);
		
    	String contextPath = url.getPath();
    	
    	String tempDir = System.getProperty("java.io.tmpdir");

    	File rootFile = new File(tempDir, "tomcat");
    	if(!rootFile.exists() && !rootFile.mkdirs()) {
    		throw new RuntimeException("Unable to create directory " + rootFile);
    	}
    	
    	File baseDir = new File(rootFile, "base");
    	if(!baseDir.exists() && !baseDir.mkdir()) {
    		throw new RuntimeException("Unable to create directory " + baseDir);
    	}
    	
    	File docBase = new File(rootFile, "doc");
    	if(!docBase.exists() && !docBase.mkdir()) {
    		throw new RuntimeException("Unable to create directory " + docBase);
    	}
    	
    	tomcat.setBaseDir(baseDir.getAbsolutePath());
    	Context context = tomcat.addContext(contextPath, docBase.getAbsolutePath());
    	
    	context.addLifecycleListener(new FixContextListener());
    	
        DispatcherServlet servlet = new DispatcherServlet(dispatcherContext);
        
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default");
		defaultServlet.setLoadOnStartup(1);
		defaultServlet.setOverridable(true);
		defaultServlet.setServlet(servlet);
		
		context.addChild(defaultServlet);
		context.addServletMappingDecoded("/", defaultServlet.getName());        

        servers.add(tomcat);
		
        start(tomcat);
        
        if(tomcat.getServer().getState() == LifecycleState.STARTED) {
            return configuration.getAll();
        }
        throw new IllegalStateException("Unable to start server");
	}

	private void start(Tomcat tomcat) throws InterruptedException, LifecycleException {
        tomcat.start();

		long deadline = System.currentTimeMillis() + 10000;
		do {
        	switch(tomcat.getServer().getState()) {
        	case NEW:
        	case INITIALIZING:
        	case INITIALIZED:
        	case STARTING_PREP:
        	case STARTING:
        		Thread.sleep(10);
        		continue;
        	default : break;
        	}
        	break;
		} while(deadline > System.currentTimeMillis());
	}
	

    private void stop(Tomcat tomcat) throws LifecycleException, InterruptedException {
		tomcat.stop();
		tomcat.destroy();
		
		long deadline = System.currentTimeMillis() + 10000;
		do {
        	switch(tomcat.getServer().getState()) {
        	case DESTROYED:
        	case FAILED:
        		return;
        	default : {
        	}
        	}
    		Thread.sleep(10);
        } while(deadline > System.currentTimeMillis());		
	}	
}
