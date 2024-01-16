package com.github.skjolber.mockito.rest.spring;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ServerSocketFactory;

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.Tomcat.FixContextListener;
import org.springframework.web.servlet.DispatcherServlet;
import org.apache.catalina.connector.Connector;

public class TomcatMockitoEndpointServerInstance implements MockitoEndpointServerInstance {

	protected boolean started = true;
	
	protected List<Tomcat> servers = new ArrayList<>();
    protected List<Context> contexts = new ArrayList<>();
    
	/**
     * 
     * Stop endpoints.
     * 
     */

    public void stop() throws Exception {
    	synchronized(this) {
	    	if(started) {
	    		started = false;
	    		for (Context context : contexts) {
	    			stop(context);
	    		}
	    		
		        for (Tomcat server : servers) {
		        	stop(server);
		        }
	    	}
    	}
    }    
    
	/**
     * 
     * Destroy endpoints.
     * 
     */

    public void destroy() throws Exception {
    	synchronized(this) {
    		started = false;
    		
    		for (Context context : contexts) {
    			stop(context);
    		}
    		
    		for (Context context : contexts) {
    			destroy(context);
    		}
    		
	        for (Tomcat server : servers) {
	        	stop(server);
	        }

	        for (Tomcat server : servers) {
	        	destroy(server);
	        }

	        contexts.clear();
	        servers.clear();
    	}
    }
    	
    /**
     * 
     * (Re)start endpoints.
     * 
     */

    public void start() throws Exception {
    	synchronized(this) {
	    	if(!started) {
	    		started = true;
		        for (Tomcat server : servers) {
		        	start(server);
		        }
	    	}
    	}
    }

	public Map<Class<?>, Object> add(List<Class<?>> mockTargetBeans, List<Class<?>> defaultContextBeans, URL url) throws Exception {
		MockitoSpringApplicationListener configuration = new MockitoSpringApplicationListener();
    	configuration.setContextBeans(defaultContextBeans);
		configuration.setMockTargetBeans(mockTargetBeans);

    	Tomcat tomcat = new Tomcat();

		File rootFile = Files.createTempDirectory("tomcat").toFile();

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

    	tomcat.setPort(url.getPort());
    	tomcat.setHostname("localhost");
		tomcat.getServer().setPort(url.getPort());
		// tomcat.getHost().setAppBase(".");

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

		Connector connector = new Connector();
		connector.setThrowOnFailure(true);
		connector.setPort(url.getPort());

		tomcat.getService().addConnector(connector);
		tomcat.setConnector(connector);

		servers.add(tomcat);
		
        start(tomcat);
        
        contexts.add(context);
        
        if(tomcat.getServer().getState() == LifecycleState.STARTED) {
            return configuration.getAll();
        }
        throw new IllegalStateException("Unable to start server");
	}

	private void start(Tomcat tomcat) throws InterruptedException, LifecycleException {
        tomcat.init();
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
		
		long deadline = System.currentTimeMillis() + 10000;
		do {
        	switch(tomcat.getServer().getState()) {
        	case STOPPED:
        	case DESTROYING:
        	case DESTROYED:
        	case FAILED:
        		return;
        	default : {
        	}
        	}
    		Thread.sleep(10);
        } while(deadline > System.currentTimeMillis());		
	}
    
	    

    private void stop(Context context) throws LifecycleException, InterruptedException {
		context.stop();

		long deadline = System.currentTimeMillis() + 10000;
		do {
        	switch(context.getState()) {
        	case STOPPED:
        	case DESTROYING:
        	case DESTROYED:
        	case FAILED:
        		return;
        	default : {
        	}
        	}
    		Thread.sleep(10);
        } while(deadline > System.currentTimeMillis());		
	}	

    private void destroy(Tomcat tomcat) throws LifecycleException, InterruptedException {
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
    
    private void destroy(Context context) throws LifecycleException, InterruptedException {
		context.destroy();

		long deadline = System.currentTimeMillis() + 10000;
		do {
        	switch(context.getState()) {
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
