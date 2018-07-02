package com.github.skjolber.mockito.rest.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * Plain java junit rule test.
 * 
 */

public class RestServiceRuleBuilderTest {

	private static String baseUrl = "http://localhost:9999/context";
	
	@Rule
	public RestServiceRule rule = RestServiceRule.newInstance();
	
	private MyRestController serviceMock;
	private RestTemplate restTemplate = new RestTemplate();
	
	@Before
	public void before() throws Exception {
		serviceMock = rule.builder(baseUrl).service(MyRestController.class).mock();
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
		ResponseEntity<String> entity = new ResponseEntity<String>(message, responseHeaders, HttpStatus.OK);
		
		when(serviceMock.method1()).thenReturn(entity);
		
		URI u1 = new URI(baseUrl + "/rest/method1");
		
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(u1, String.class);
		
		assertThat(responseEntity.getBody(), is(message));
	}
	

}
