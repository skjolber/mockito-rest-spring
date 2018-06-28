package com.github.skjolber.mockito.rest.spring;

import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyMockitoSpringConfiguration extends AbstractConfiguration  {

	protected MockitoSpringConfiguration mockitoSpringConfiguration;
	
	public JettyMockitoSpringConfiguration(MockitoSpringConfiguration mockitoSpringConfiguration) {
		this.mockitoSpringConfiguration = mockitoSpringConfiguration;
	}

	@Override
	public void configure(WebAppContext context) throws Exception {
		//add a bean to the context which will call the servletcontainerinitializers when appropriate
		JettyMockitoSpringContainerStarter starter = new JettyMockitoSpringContainerStarter(context, mockitoSpringConfiguration.getMockTargetBeans(), mockitoSpringConfiguration.getContextBeans(), mockitoSpringConfiguration);
        context.addBean(starter, true);
	}
	
	
}
