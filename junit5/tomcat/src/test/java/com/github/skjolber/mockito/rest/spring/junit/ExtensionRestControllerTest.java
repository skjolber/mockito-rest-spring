package com.github.skjolber.mockito.rest.spring.junit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.github.skjolber.mockito.rest.spring.MockitoEndpointExtension;
import com.github.skjolber.mockito.rest.spring.MyRestController;
import com.github.skjolber.mockito.rest.spring.api.MockEndpoint;

/**
 * 
 * Test using a single {@link RestController}.
 * 
 */

@ExtendWith(MockitoEndpointExtension.class)
public class ExtensionRestControllerTest {

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
		
		when(serviceMock.method1()).thenReturn(ResponseEntity.ok().body(message));
		
		URI u1 = new URI("http://localhost:" + MockitoEndpointExtension.getPort() + "/rest/method1");
		
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(u1, String.class);
		
		assertThat(responseEntity.getBody(), is(message));
	}

}
