package com.github.skjolber.mockito.rest.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;

/**
 * 
 * Plain java junit rule test.
 * 
 */

public class RestServiceRule1Test {

	private static String baseUrl = "http://localhost:9999/context";
	
	@Rule
	public RestServiceRule rule = RestServiceRule.newInstance();
	
	private MyRestController serviceMock;
	private RestTemplate restTemplate = new RestTemplate();
	
	private ResponseEntityMapper mapper = new ResponseEntityMapper();

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
		
		ResponseEntity<String> entity = new ResponseEntity<String>(message, HttpStatus.OK);
		
		when(serviceMock.method1()).thenReturn(entity);
		
		URI u1 = new URI(baseUrl + "/rest/method1");
		
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(u1, String.class);
		
		assertThat(responseEntity.getBody(), is(message));
	}
	
	/**
	 * GET method returning a JSON-annotated object.
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void testMethod2() throws Exception {
		MyResponse expected = new MyResponse();
		expected.setCode(0);
		expected.setValue("abcdef");
		
		when(serviceMock.method2()).thenReturn(expected);
		
		URI u1 = new URI(baseUrl + "/rest/method2");
		
		ResponseEntity<MyResponse> responseEntity = restTemplate.getForEntity(u1, MyResponse.class);

		MyResponse body = responseEntity.getBody();
		
		assertThat(body.getCode(), is(expected.getCode()));
		assertThat(body.getValue(), is(expected.getValue()));
	}
	
	/**
	 * POST method for JSON-annotated request and response objects.
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void testMethod3() throws Exception {
		MyResponse expected = new MyResponse();
		expected.setCode(0);
		expected.setValue("abc");
		
		when(serviceMock.method3(ArgumentMatchers.any(MyRequest.class))).thenReturn(expected);
		
		URI u1 = new URI(baseUrl + "/rest/method3");
		
		MyRequest request = new MyRequest();
		request.setCode(1);
		request.setValue("abc");
		
		ResponseEntity<MyResponse> responseEntity = restTemplate.postForEntity(u1, request, MyResponse.class);

		MyResponse body = responseEntity.getBody();
		
		assertThat(body.getCode(), is(expected.getCode()));
		assertThat(body.getValue(), is(expected.getValue()));
		
		ArgumentCaptor<MyRequest> argument1 = ArgumentCaptor.forClass(MyRequest.class);
		verify(serviceMock, times(1)).method3(argument1.capture());
		
		MyRequest value = argument1.getValue();
		
		assertThat(value.getCode(), is(request.getCode()));
		assertThat(value.getValue(), is(request.getValue()));
	}
	
	/**
	 * GET method returning an {@linkplain ResponseEntity} using the response entity mapper.
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void testMethod4() throws Exception {
		when(serviceMock.method4()).thenReturn(mapper.response("/examples/myResponse.json", MyResponse.class));
		
		URI u1 = new URI(baseUrl + "/rest/method4");
		
		ResponseEntity<MyResponse> responseEntity = restTemplate.getForEntity(u1, MyResponse.class);

		MyResponse body = responseEntity.getBody();
		
		assertThat(body.getCode(), is(0));
		assertThat(body.getValue(), is("ghi"));
	}

	@Test
	public void testMethodWithResponseHeaders() throws Exception {
		when(serviceMock.method4()).thenReturn(mapper.response("/examples/myResponse.json", MyResponse.class, "myHeader", "myValue"));
		
		URI u1 = new URI(baseUrl + "/rest/method4");
		
		Thread.sleep(1000);

		ResponseEntity<MyResponse> responseEntity = restTemplate.getForEntity(u1, MyResponse.class);

		MyResponse body = responseEntity.getBody();
		
		assertThat(body.getCode(), is(0));
		assertThat(body.getValue(), is("ghi"));
		assertThat(responseEntity.getHeaders().get("myHeader").get(0), is("myValue"));
	}
	
	/**
	 * 
	 * Some simple exception handling test.
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void testMethod1Exception() throws Exception {
		when(serviceMock.method1()).thenThrow(new MyException());
		when(serviceMock.handleException(ArgumentMatchers.any(MyException.class))).thenCallRealMethod();
		
		URI u1 = new URI(baseUrl + "/rest/method1");
		
		RequestEntity<?> requestEntity = new RequestEntity<>(HttpMethod.GET, u1);
		
		try {
			ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
			
			Assert.fail("Unexpectedly got '" + responseEntity.getBody() + "'");
		} catch (HttpStatusCodeException exception) {
		    int statusCode = exception.getStatusCode().value();
		    // TODO get error object here
		    
		    assertThat(statusCode, is(400));
		}
	}
	
	@After
	public void reset() {
		Mockito.reset(serviceMock);
	}
}
