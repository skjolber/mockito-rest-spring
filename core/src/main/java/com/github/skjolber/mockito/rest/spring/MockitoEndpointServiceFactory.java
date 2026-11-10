package com.github.skjolber.mockito.rest.spring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;

public class MockitoEndpointServiceFactory {

	protected List<Class<?>> beans = new ArrayList<>();
	// unsealed, so that generated classes can be defined directly via the class loader itself
	protected ClassLoader classLoader = new ByteArrayClassLoader(getClass().getClassLoader(), false, Collections.emptyMap());

	public <T> Class<T> add(Class<T> serviceClass) throws Exception {
		return add(serviceClass, null);
	}

	public <T> Class<T> add(Class<T> serviceInterface, String path) throws Exception {

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
		Unloaded<T> unloaded = subclass.make();
		if(classLoader instanceof InjectionClassLoader injectionClassLoader && !injectionClassLoader.isSealed()) {
			// define the class using the class loader's own (protected) defineClass method;
			// avoids reflective injection, which requires sun.misc.Unsafe and is disabled by Byte Buddy from Java 26
			return (Class<T>) unloaded.load(injectionClassLoader, InjectionClassLoader.Strategy.INSTANCE).getLoaded();
		}
		// custom class loader (i.e. subclass): fall back to reflective injection
		return (Class<T>) unloaded.load(classLoader, ClassLoadingStrategy.Default.INJECTION).getLoaded();
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public List<Class<?>> getBeans() {
		return beans;
	}
}
