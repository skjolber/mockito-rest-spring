package com.github.skjolber.mockito.rest.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import com.github.skjolber.mockito.rest.spring.config.LoggingSpringWebMvcConfig;

/**
 * 
 * Spring context rule test. This tests that having two separate Spring contexts in the same JVM works.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/spring/beans.xml"})
public class RestServiceRule2Test {

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Rule // with example of custom configuration
	public RestServiceRule rule = RestServiceRule.newInstance(Arrays.<Class<?>>asList(LoggingSpringWebMvcConfig.class));

	/**
	 * Endpoint address (full url), typically pointing to localhost for unit testing, remote host otherwise.
	 */

	@Value("${my.service}")
	private String baseUrl;
	
	private MyRestController serviceMock;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Before
	public void before() throws Exception {
		serviceMock = rule.mock(MyRestController.class, baseUrl);
	}
	
	/**
	 * GET method returning an {@linkplain ResponseEntity}. 
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void testMethod1() throws Exception {
		String message = "abc";
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("MyResponseHeader", "MyValue");
		ResponseEntity entity = new ResponseEntity<String>(message, responseHeaders, HttpStatus.OK);
		
		when(serviceMock.method1()).thenReturn(entity);
		
		URL u1 = new URL(baseUrl + "/rest/method1");
		
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(u1.toURI(), String.class);
		
		assertThat(responseEntity.getBody(), is(message));
	}
	

}
