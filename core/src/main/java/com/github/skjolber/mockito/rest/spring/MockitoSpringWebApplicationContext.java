package com.github.skjolber.mockito.rest.spring;

import java.util.List;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class MockitoSpringWebApplicationContext extends AnnotationConfigWebApplicationContext {

	protected List<Class<?>> beans;
	
	public MockitoSpringWebApplicationContext(List<Class<?>> beans) {
		this.beans = beans;
	}

	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
		super.loadBeanDefinitions(beanFactory);
		
		for(int i = 0; i < beans.size(); i++) {
			GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
			beanDefinition.setBeanClass(MockitoSpringFactoryBean.class);
			beanDefinition.setLazyInit(false);
			beanDefinition.setAbstract(false);
			beanDefinition.setAutowireCandidate(true);
			
			MutablePropertyValues propertyValues = new MutablePropertyValues();
			propertyValues.addPropertyValue("className", beans.get(i).getName());
			beanDefinition.setPropertyValues(propertyValues);
	
			beanFactory.registerBeanDefinition(MockitoSpringFactoryBean.class.getSimpleName() + i, beanDefinition);
		}
	}
	
	public Class<?> findClass(String name) {
		for (Class<?> c : beans) {
			if(name.equals(c.getName())) {
				return c;
			}
		}
		return null;
	}
}
