package com.github.skjolber.mockito.rest.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.github.skjolber.mockito.rest.spring.api.MockEndpoint;

/**
 * 
 * Plain java junit rule test.
 * 
 */

@ExtendWith(MockitoEndpointExtension.class)
public class ExtensionTest {

	private static String baseUrl = "http://localhost:9999";
	
	private RestTemplate restTemplate = new RestTemplate();

	@MockEndpoint
	private MyRestController serviceMock;
	/**
	 * GET method returning an {@linkplain ResponseEntity}.
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void testMethod1() throws Exception {
		String message = "abc";
		
		ResponseEntity<String> entity = new ResponseEntity<String>(message, HttpStatus.OK);
		
		when(serviceMock.method1()).thenReturn(entity);
		
		URL u1 = new URL(baseUrl + "/rest/method1");
		
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(u1.toURI(), String.class);
		
		assertThat(responseEntity.getBody(), is(message));
	}
	
	
	

}
