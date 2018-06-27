package com.github.skjolber.mockito.rest.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType.Loaded;

public class MockitoEndpointServiceFactory {
	
	protected List<Class<?>> beans = new ArrayList<>();
	
	public <T> Class<T> service(Class<T> serviceClass) throws Exception {
		return service(serviceClass, null);
	}

	public <T> Class<T> service(Class<T> serviceInterface, String path) throws Exception {
		
		if(path != null || serviceInterface.isInterface()) {
			Class<T> asService = asService(serviceInterface, path);
			beans.add(asService);
			
			return asService;
		} 
		
		beans.add(serviceInterface);
		return serviceInterface;
	}

    protected <T> Class<T> asService(Class<T> serviceInterface) throws Exception {
    	return asService(serviceInterface, null);
    }

    protected <T> Class<T> asService(Class<T> serviceInterface, String path) throws Exception {
    	
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
}
