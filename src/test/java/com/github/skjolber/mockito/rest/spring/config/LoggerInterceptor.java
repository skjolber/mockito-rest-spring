package com.github.skjolber.mockito.rest.spring.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler. HandlerInterceptorAdapter;

/**
 * 
 * Some simple logging for serverside. In most cases, adding logging to the client side in addition makes sense.
 * 
 */

public class LoggerInterceptor extends HandlerInterceptorAdapter {

	private static Logger logger = LoggerFactory.getLogger(LoggerInterceptor.class);
	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		logger.info("Before server handling the request");
		
		return super.preHandle(request, new HttpServletResponseWrapper((HttpServletResponse) response), handler);
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		logger.info("After server handling the request");

		super.postHandle(request, response, handler, modelAndView);
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		logger.info("After server rendering the view");
		
		super.afterCompletion(request, response, handler, ex);
	}
}