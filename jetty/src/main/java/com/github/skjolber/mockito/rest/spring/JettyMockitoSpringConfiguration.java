package com.github.skjolber.mockito.rest.spring;

import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.ee10.webapp.AbstractConfiguration;

public class JettyMockitoSpringConfiguration extends AbstractConfiguration  {

	protected MockitoSpringApplicationListener configuration;

	public JettyMockitoSpringConfiguration(MockitoSpringApplicationListener configuration) {
		super(new Builder());
		this.configuration = configuration;
	}

	@Override
	public void configure(WebAppContext context) throws Exception {
		//add a bean to the context which will call the servletcontainerinitializers when appropriate
		JettyMockitoSpringContainerStarter starter = new JettyMockitoSpringContainerStarter(context, configuration.getMockTargetBeans(), configuration.getContextBeans(), configuration);
		context.addBean(starter, true);
	}


}
