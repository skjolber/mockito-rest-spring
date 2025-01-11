package com.github.skjolber.mockito.rest.spring.mockito;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.github.skjolber.mockito.rest.spring.api.MockEndpoint;

public class MockEndpointFieldHelper {
	private final Object instance;
	private final Class<?> clazz;

	public MockEndpointFieldHelper(Object instance, Class<?> clazz) {
		this.instance = instance;
		this.clazz = clazz;
	}

	public Set<Field> getFields() {
		Set<Field> annotatedFields = new HashSet<>();

		Class<?> current = clazz;
		do {
			for (Field field : current.getDeclaredFields()) {
				if (field.isAnnotationPresent(MockEndpoint.class)) {
					annotatedFields.add(field);
				}
			}
			current = current.getSuperclass();
			if(current == Object.class) {
				break;
			}
		} while(true);
		return annotatedFields;

	}

	public void setField(Field field, Object value) {
		try {
			field.setAccessible(true);
			field.set(instance, value);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set " + field.getName() + " of object " + value.getClass().getName(), e);
		}
	}

}