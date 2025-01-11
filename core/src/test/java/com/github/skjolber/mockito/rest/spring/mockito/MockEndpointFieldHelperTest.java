package com.github.skjolber.mockito.rest.spring.mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.skjolber.mockito.rest.spring.api.MockEndpoint;

public class MockEndpointFieldHelperTest {

	private static class Parent {
		@MockEndpoint
		private Object objecet;
	}

	private static class Child extends Parent {
	}

	@Test
	public void testParent() {
		MockEndpointFieldHelper h = new MockEndpointFieldHelper(new Parent(), Parent.class);
		
		Set<Field> fields = h.getFields();
		assertEquals(1, fields.size());
	}
	
	@Test
	public void testChild() {
		MockEndpointFieldHelper h = new MockEndpointFieldHelper(new Child(), Child.class);
		
		Set<Field> fields = h.getFields();
		assertEquals(1, fields.size());
	}
	
}
