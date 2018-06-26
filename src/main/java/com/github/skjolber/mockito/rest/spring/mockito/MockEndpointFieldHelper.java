package com.github.skjolber.mockito.rest.spring.mockito;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.mockito.internal.util.reflection.FieldSetter;

import com.github.skjolber.mockito.rest.spring.api.MockEndpoint;

/**
 * Scan mocks, and prepare them if needed.
 */
public class MockEndpointFieldHelper {
    private final Object instance;
    private final Class<?> clazz;

    /**
     * Creates a MockScanner.
     *
     * @param instance The test instance
     * @param clazz    The class in the type hierarchy of this instance.
     */
    public MockEndpointFieldHelper(Object instance, Class<?> clazz) {
        this.instance = instance;
        this.clazz = clazz;
    }

    /**
     * Scan and prepare mocks for the given <code>testClassInstance</code> and <code>clazz</code> in the type hierarchy.
     *
     * @return A prepared set of mock
     */
    public Set<Field> getFields() {
        Set<Field> annotatedFields = new HashSet<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(MockEndpoint.class)) {
                annotatedFields.add(field);
            }
        }
        return annotatedFields;
    }
    
    public void setField(Field field, Object value) {
    	FieldSetter.setField(instance, field, value);
    }

}