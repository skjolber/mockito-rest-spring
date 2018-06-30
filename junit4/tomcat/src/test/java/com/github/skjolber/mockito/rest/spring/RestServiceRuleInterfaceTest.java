package com.github.skjolber.mockito.rest.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.swagger.api.PetApi;
import io.swagger.model.Pet;

/**
 * 
 * Plain java junit rule test for swagger-generated stub.
 * 
 */

public class RestServiceRuleInterfaceTest {

	private static String baseUrl = "http://localhost:9999/context";
	
	@Rule
	public RestServiceRule rule = RestServiceRule.newInstance();
	
	private PetApi serviceMock;
	private RestTemplate restTemplate = new RestTemplate();
	
	@Before
	public void before() throws Exception {
		// mock interface as endpoint with specific path
		serviceMock = rule.mock(PetApi.class, baseUrl, "/rest");
	}

	/**
	 * GET method returning an {@linkplain ResponseEntity}.
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void testMethod1() throws Exception {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", "application/json");
		Pet outputPet = new Pet();
		outputPet.setId(3L);
		outputPet.setName("response");
		ResponseEntity<Pet> entity = new ResponseEntity<Pet>(outputPet, responseHeaders, HttpStatus.OK);

		when((serviceMock).addPet(ArgumentMatchers.any(Pet.class))).thenReturn(entity);

		URI u1 = new URI(baseUrl + "/rest/pet");

		Pet inputPet = new Pet();
		inputPet.setName("input");

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON); // avoid 415, this can probably be skipped in a future version of swagger-codegen
		
		ResponseEntity<Pet> responseEntity = restTemplate.exchange(u1, HttpMethod.POST, new HttpEntity<>(inputPet, headers), Pet.class);
		
		assertThat(responseEntity.getBody().getId(), is(outputPet.getId()));

		ArgumentCaptor<Pet> argument1 = ArgumentCaptor.forClass(Pet.class);
		verify(serviceMock, times(1)).addPet(argument1.capture());
		
		assertThat(argument1.getValue().getName(), is(inputPet.getName()));
	}
	
}
