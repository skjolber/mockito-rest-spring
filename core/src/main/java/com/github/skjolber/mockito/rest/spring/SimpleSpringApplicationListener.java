package com.github.skjolber.mockito.rest.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Capture mocked beans.
 *
 */

public class SimpleSpringApplicationListener implements ApplicationListener<ApplicationContextEvent> {

	private ApplicationContextEvent event;
	
	public void onApplicationEvent(ApplicationContextEvent event) {
		this.event = event;
		System.out.println(event);
		synchronized(this) {
			this.notifyAll();
		}
	}
	
	public ApplicationContextEvent getEvent() {
		return event;
	}
	
	
}
