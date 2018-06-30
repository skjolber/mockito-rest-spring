package com.github.skjolber.mockito.rest.spring;

import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyMockitoSpringConfiguration extends AbstractConfiguration  {

	protected MockitoSpringApplicationListener configuration;
	
	public JettyMockitoSpringConfiguration(MockitoSpringApplicationListener configuration) {
		this.configuration = configuration;
	}

	@Override
	public void configure(WebAppContext context) throws Exception {
		//add a bean to the context which will call the servletcontainerinitializers when appropriate
		JettyMockitoSpringContainerStarter starter = new JettyMockitoSpringContainerStarter(context, configuration.getMockTargetBeans(), configuration.getContextBeans(), configuration);
        context.addBean(starter, true);
	}
	
	
}
