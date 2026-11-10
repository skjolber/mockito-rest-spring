package com.github.skjolber.mockito.rest.spring;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public class MockitoEndpointServiceFactoryTest {

	public interface MyService {
		String hello();
	}

	@Test
	public void testInterfaceWithPath() throws Exception {
		MockitoEndpointServiceFactory factory = new MockitoEndpointServiceFactory();

		Class<MyService> service = factory.add(MyService.class, "/api");

		assertNotSame(MyService.class, service);
		assertTrue(MyService.class.isAssignableFrom(service));
		assertSame(factory.getClassLoader(), service.getClassLoader());
		assertTrue(service.isAnnotationPresent(RestController.class));
		assertArrayEquals(new String[] {"/api"}, service.getAnnotation(RequestMapping.class).path());
		assertTrue(factory.getBeans().contains(service));
	}

	@Test
	public void testMultipleServicesInSameClassLoader() throws Exception {
		MockitoEndpointServiceFactory factory = new MockitoEndpointServiceFactory();

		Class<MyService> first = factory.add(MyService.class);
		Class<MyService> second = factory.add(MyService.class, "/other");

		assertNotSame(first, second);
		assertSame(factory.getClassLoader(), first.getClassLoader());
		assertSame(factory.getClassLoader(), second.getClassLoader());
	}
}
