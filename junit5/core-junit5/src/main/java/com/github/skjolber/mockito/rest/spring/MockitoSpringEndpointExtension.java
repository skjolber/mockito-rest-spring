package com.github.skjolber.mockito.rest.spring;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 
 * This extension combines {@linkplain SpringExtension} and {@linkplain TenantJsonWebToken}; in the correct order.
 *
 */

public class MockitoSpringEndpointExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor,
    BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback,
    ParameterResolver {
    
    private SpringExtension springExtension = new SpringExtension();
    private MockitoEndpointExtension mockitoEndpointExtension = new MockitoEndpointExtension();
    
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return springExtension.supportsParameter(parameterContext, extensionContext);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return springExtension.resolveParameter(parameterContext, extensionContext);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
    	mockitoEndpointExtension.afterTestExecution(context);
        springExtension.afterTestExecution(context);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
    	mockitoEndpointExtension.beforeTestExecution(context);
        springExtension.beforeTestExecution(context);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        mockitoEndpointExtension.afterEach(context);
        springExtension.afterEach(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        mockitoEndpointExtension.beforeEach(context);
        springExtension.beforeEach(context);        
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    	mockitoEndpointExtension.postProcessTestInstance(testInstance, context);
        springExtension.postProcessTestInstance(testInstance, context);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        mockitoEndpointExtension.afterAll(context); 
        springExtension.afterAll(context);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        mockitoEndpointExtension.beforeAll(context);
        springExtension.beforeAll(context);
    }

}
